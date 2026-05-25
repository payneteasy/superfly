package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.request.TouchSessionsRequest;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rpuch
 */
public class SessionToucherImplTest {
    private SSOService ssoService;

    @Before
    public void setUp() {
        ssoService = EasyMock.createStrictMock(SSOService.class);
    }

    @Test
    public void testSingle() throws InterruptedException {
        SessionToucherImpl toucher = new SessionToucherImpl(1); // 1 second between flushes
        toucher.setSsoService(ssoService);
        toucher.startup();

        ssoService.touchSessions(TouchSessionsRequest.builder().sessionIds(List.of(10L)).build());
        EasyMock.expectLastCall();
        EasyMock.replay(ssoService);
        toucher.addSessionId(10L);
        Thread.sleep(1500); // timeout, so touch arrives
        EasyMock.verify(ssoService);

        // testing empty result - no send!
        EasyMock.reset(ssoService);
        EasyMock.replay(ssoService);
        Thread.sleep(1500); // timeout, so touch arrives
        EasyMock.verify(ssoService);

        toucher.shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisable() throws InterruptedException {
        SessionToucherImpl toucher = new SessionToucherImpl(1); // 1 second between flushes
        toucher.setEnabled(false);
        toucher.setSsoService(ssoService);
        toucher.startup();

        // nothing should be sent
        final Set<Boolean> sent = new HashSet<Boolean>();
        ssoService.touchSessions(TouchSessionsRequest.builder().sessionIds(EasyMock.anyObject(List.class)).build());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                sent.add(true);
                return null;
            }
        }).anyTimes()
        ;
        EasyMock.replay(ssoService);
        toucher.addSessionId(10L);
        Thread.sleep(1500); // timeout, so touch arrives
        EasyMock.verify(ssoService);

        toucher.shutdown();

        Assert.assertTrue("Nothing should be sent", sent.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultithreaded() throws InterruptedException {
        final Set<Long> touchedSessionIds = Collections.synchronizedSet(new HashSet<>());

        ssoService.touchSessions(TouchSessionsRequest.builder().sessionIds(EasyMock.anyObject(List.class)).build());

        EasyMock.expectLastCall()
                .andAnswer(() -> {
                    TouchSessionsRequest currentArgument = (TouchSessionsRequest) EasyMock.getCurrentArguments()[0];
                    List<Long>           ids             = currentArgument.getSessionIds();
                    touchedSessionIds.addAll(ids);
                    return null;
                })
                .anyTimes()
        ;
        EasyMock.replay(ssoService);

        final SessionToucherImpl toucher = new SessionToucherImpl(1); // 1 second between flushes
        toucher.setSsoService(ssoService);
        toucher.startup();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final long ii = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    toucher.addSessionId(ii);
                }
            });
            threads[i].start();
        }

        // waiting for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        Thread.sleep(3000); // timeout, so touch arrives
        toucher.shutdown();
        EasyMock.verify(ssoService);

        Set<Long> expectedIds = new HashSet<Long>();
        for (long i = 0; i < 10; i++) {
            expectedIds.add(i);
        }
        Assert.assertEquals(expectedIds, touchedSessionIds);
    }
}
