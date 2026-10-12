package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.common.utils.LogSanitizer;
import com.payneteasy.superfly.dao.EventDao;
import com.payneteasy.superfly.model.Event;
import com.payneteasy.superfly.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.LongSupplier;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
public class EventServiceImpl implements EventService, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);


    private EventDao eventDao;
    private static final int DELAY_TIME_MS = 500;
    private static final int EVENTS_LIMIT = 200;
    // the paynet client waits for the response for 90 s
    static final long MAX_WAIT_TIME_MS = 75_000;
    // each waiting poll holds a request thread; excess polls get an empty result at once
    static final long OVER_LIMIT_PAUSE_MS = 1000;
    static final int MAX_WAITING_TOTAL = 50;
    static final int MAX_WAITING_PER_SUBSYSTEM = 4;
    private final Semaphore totalWaiting = new Semaphore(MAX_WAITING_TOTAL);
    private final ConcurrentHashMap<String, Semaphore> subsystemWaiting = new ConcurrentHashMap<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    // test seams
    LongSupplier clock = System::currentTimeMillis;
    Sleeper sleeper = Thread::sleep;

    @Override
    public void destroy() throws Exception {
        isShutdown.set(true);
    }

    @Autowired
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    // long-polling: a transaction held while sleeping would pin a REPEATABLE READ snapshot
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Event> getEvents(Long lastEventId, long waitTimeMs, String subsystemName) {
        List<Event> events = eventDao.getEvents(lastEventId, EVENTS_LIMIT, subsystemName);
        List<Event> result = (events != null ? new ArrayList<>(events) : new ArrayList<>());
        if (result.isEmpty() && waitTimeMs > 0) {
            if (!waitWithPermits(lastEventId, waitTimeMs, subsystemName, result)) {
                pauseOverLimit(waitTimeMs);
            }
        }
        return result;
    }

    /** @return false when the poll was over the limit and did not wait */
    private boolean waitWithPermits(Long lastEventId, long waitTimeMs, String subsystemName, List<Event> result) {
        Semaphore perSubsystem = subsystemWaiting.computeIfAbsent(String.valueOf(subsystemName),
                k -> new Semaphore(MAX_WAITING_PER_SUBSYSTEM));
        if (!perSubsystem.tryAcquire()) {
            logger.warn("Too many concurrent event polls for subsystem {}, returning without waiting",
                    LogSanitizer.forLog(subsystemName));
            return false;
        }
        try {
            if (!totalWaiting.tryAcquire()) {
                logger.warn("Too many concurrent event polls, returning without waiting for subsystem {}",
                        LogSanitizer.forLog(subsystemName));
                return false;
            }
            try {
                waitForEvents(lastEventId, waitTimeMs, subsystemName, result);
            } finally {
                totalWaiting.release();
            }
        } finally {
            perSubsystem.release();
        }
        return true;
    }

    // a client without backoff would otherwise re-poll (and hit the DB) in a tight loop; no permits are held here
    private void pauseOverLimit(long waitTimeMs) {
        long pause = Math.min(OVER_LIMIT_PAUSE_MS, Math.min(waitTimeMs, MAX_WAIT_TIME_MS));
        try {
            sleeper.sleep(pause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForEvents(Long lastEventId, long waitTimeMs, String subsystemName, List<Event> result) {
        long now = clock.getAsLong();
        long finishTime = now + Math.min(waitTimeMs, MAX_WAIT_TIME_MS);
        while ((now < finishTime) && !isShutdown.get()) {
            try {
                sleeper.sleep(DELAY_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            final List<Event> newEvents = eventDao.getEvents(lastEventId, EVENTS_LIMIT, subsystemName);

            if (newEvents != null && !newEvents.isEmpty()) {
                result.addAll(newEvents);
                break;
            }
            now = clock.getAsLong();
        }
    }

    @Override
    public long getLastEventId(String subsystemName) {
        return eventDao.getLastEventId(subsystemName);
    }
}
