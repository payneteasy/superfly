package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.EventDao;
import com.payneteasy.superfly.model.Event;
import com.payneteasy.superfly.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Transactional
public class EventServiceImpl implements EventService, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);


    private EventDao eventDao;
    private static final int DELAY_TIME_MS = 500;
    private static final int EVENTS_LIMIT = 200;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    @Override
    public void destroy() throws Exception {
        isShutdown.set(true);
    }

    @Required
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public List<Event> getEvents(Date lastEventTime, long waitTimeMs) {
        List<Event> events = eventDao.getEvents(lastEventTime, EVENTS_LIMIT);
        List<Event> result = (events !=null ? new ArrayList<>(events) : new ArrayList<>());
        if (result.isEmpty()) {
            long now = System.currentTimeMillis();
            long finishTime = now + waitTimeMs;
            while ((now < finishTime) && !isShutdown.get()) {
                try {
                    Thread.sleep(DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                final List<Event> newEvents = eventDao.getEvents(lastEventTime , EVENTS_LIMIT);

                if (newEvents!=null && !newEvents.isEmpty()) {
                    result.addAll(newEvents);
                    break;
                }
                now = System.currentTimeMillis();
            }
        }
        return result;
    }
}
