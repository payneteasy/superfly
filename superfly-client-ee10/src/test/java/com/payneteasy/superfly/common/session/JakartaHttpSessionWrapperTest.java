package com.payneteasy.superfly.common.session;

import jakarta.servlet.http.HttpSession;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class JakartaHttpSessionWrapperTest {

    private static final Logger logger = LoggerFactory.getLogger(JakartaHttpSessionWrapperTest.class);

    private HttpSession mockSession;
    private JakartaHttpSessionWrapper wrapper;

    @Before
    public void setUp() {
        mockSession = EasyMock.createMock(HttpSession.class);
        wrapper = new JakartaHttpSessionWrapper(mockSession);
    }

    @After
    public void tearDown() {
        verify(mockSession);
    }

    @Test
    public void testGetId() {
        logger.debug("Test: {}", "getId");
        expect(mockSession.getId()).andReturn("session-id-456");
        replay(mockSession);

        assertEquals("session-id-456", wrapper.getId());
    }

    @Test
    public void testGetAttribute() {
        logger.debug("Test: {}", "getAttribute");
        expect(mockSession.getAttribute("key")).andReturn("value");
        replay(mockSession);

        assertEquals("value", wrapper.getAttribute("key"));
    }

    @Test
    public void testSetAttribute() {
        logger.debug("Test: {}", "setAttribute");
        mockSession.setAttribute("key", "value");
        EasyMock.expectLastCall();
        replay(mockSession);

        wrapper.setAttribute("key", "value");
    }

    @Test
    public void testRemoveAttribute() {
        logger.debug("Test: {}", "removeAttribute");
        mockSession.removeAttribute("key");
        EasyMock.expectLastCall();
        replay(mockSession);

        wrapper.removeAttribute("key");
    }

    @Test
    public void testInvalidate() {
        logger.debug("Test: {}", "invalidate");
        mockSession.invalidate();
        EasyMock.expectLastCall();
        replay(mockSession);

        wrapper.invalidate();
    }

    @Test
    public void testGetOriginalSession() {
        logger.debug("Test: {}", "getOriginalSession");
        replay(mockSession);

        assertSame(mockSession, wrapper.getOriginalSession());
    }
}
