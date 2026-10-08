package com.payneteasy.superfly.service.impl;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.payneteasy.superfly.service.SessionService;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class InternalSSOServiceTouchSessionsTest {

    private SessionService sessionService;
    private InternalSSOServiceImpl service;

    @Before
    public void setUp() {
        sessionService = createStrictMock(SessionService.class);
        service = new InternalSSOServiceImpl();
        service.setSessionService(sessionService);
        replay(sessionService);
    }

    @Test
    public void callerWithoutSubsystemTouchesNothing() {
        service.touchSessions(Arrays.asList(1L, 2L), null);

        verify(sessionService);
    }
}
