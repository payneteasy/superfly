package com.payneteasy.superfly.web.controller.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.web.security.SubsystemAuthenticationToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RemoteApiControllerHardeningTest {

    private SSOService service;
    private MockMvc mvc;
    private ListAppender<ILoggingEvent> logs;
    private Logger controllerLogger;
    private Level oldLevel;

    @Before
    public void setUp() {
        service = createMock(SSOService.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new RemoteApiController(service, new ApiSerializationManager())).build();
        SecurityContextHolder.getContext().setAuthentication(
                new SubsystemAuthenticationToken("subsystem-a", null, List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM"))));
        controllerLogger = (Logger) LoggerFactory.getLogger(RemoteApiController.class);
        oldLevel = controllerLogger.getLevel();
        controllerLogger.setLevel(Level.DEBUG);
        logs = new ListAppender<>();
        logs.start();
        controllerLogger.addAppender(logs);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
        controllerLogger.detachAppender(logs);
        controllerLogger.setLevel(oldLevel);
    }

    @Test
    public void wildcardAcceptReturnsJson() throws Exception {
        expect(service.getLastEventId()).andReturn(42L);
        replay(service);

        MvcResult result = call("getLastEventId", "null", "*/*");

        assertEquals(200, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType(), result.getResponse().getContentType().startsWith("application/json"));
        assertEquals("42", result.getResponse().getContentAsString());
        verify(service);
    }

    @Test
    public void acceptListWithJsonReturnsJson() throws Exception {
        expect(service.getLastEventId()).andReturn(42L);
        replay(service);

        MvcResult result = call("getLastEventId", "null", "text/html, application/json;q=0.9");

        assertEquals(200, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith("application/json"));
        verify(service);
    }

    @Test
    public void unsupportedAcceptIsRejectedWithoutInvokingTheMethod() throws Exception {
        replay(service);

        MvcResult result = call("getLastEventId", "null", "text/xml");

        assertEquals(406, result.getResponse().getStatus());
        verify(service);
    }

    @Test
    public void malformedAcceptIsRejectedWithoutInvokingTheMethod() throws Exception {
        replay(service);

        assertEquals(406, call("getLastEventId", "null", "not a media type").getResponse().getStatus());
        verify(service);
    }

    @Test
    public void invalidJsonGivesGenericErrorWithoutParserDetails() throws Exception {
        replay(service);

        MvcResult result = call("getUserDescription", "{\"a\":", "application/json");

        String body = result.getResponse().getContentAsString();
        assertEquals(body, 500, result.getResponse().getStatus());
        assertTrue(body, body.contains("errorId: "));
        assertFalse(body, body.contains("JsonSyntaxException"));
        assertFalse(body, body.contains("EOF"));
        assertFalse(body, body.contains("$."));
        assertFalse(body, body.contains("gson"));
    }

    @Test
    public void unknownMethodGivesGenericError() throws Exception {
        replay(service);

        String body = call("noSuchMethod", "{}", "application/json").getResponse().getContentAsString();

        assertTrue(body, body.contains("errorId: "));
        assertFalse(body, body.contains("IllegalStateException"));
        assertFalse(body, body.contains("noSuchMethod"));
    }

    @Test
    public void nonContractServiceExceptionIsHiddenAndLoggedWithErrorId() throws Exception {
        service.resetPassword(anyObject());
        expectLastCall().andThrow(new IllegalStateException("could not execute procedure ss_secret_proc: Access denied"));
        replay(service);

        MvcResult result = call("resetPassword", "{\"username\":\"u\"}", "application/json");

        String body = result.getResponse().getContentAsString();
        assertFalse(body, body.contains("ss_secret_proc"));
        assertFalse(body, body.contains("IllegalStateException"));
        assertFalse(body, body.contains("Exception: "));
        String errorId = body.replaceAll("(?s).*errorId: ([0-9a-f-]{36}).*", "$1");
        assertEquals(body, 36, errorId.length());
        assertTrue("error with the cause must be logged under the errorId", logs.list.stream().anyMatch(e ->
                e.getLevel() == Level.ERROR && e.getFormattedMessage().contains(errorId)
                        && e.getThrowableProxy() != null
                        && e.getThrowableProxy().getMessage().contains("ss_secret_proc")));
    }

    @Test
    public void contractExceptionKeepsClassAndMessage() throws Exception {
        service.resetPassword(anyObject());
        expectLastCall().andThrow(new UserNotFoundException("User u not found"));
        replay(service);

        MvcResult result = call("resetPassword", "{\"username\":\"u\"}", "application/json");

        String body = result.getResponse().getContentAsString();
        assertEquals(body, 202, result.getResponse().getStatus());
        assertTrue(body, body.contains("\"exceptionClass\":\"com.payneteasy.superfly.api.UserNotFoundException\""));
        assertTrue(body, body.contains("\"message\":\"User u not found\""));
        assertTrue(body, body.contains("\"detailMessage\":null"));
    }

    @Test
    public void requestArgumentsAreNotLogged() throws Exception {
        expect(service.authenticate(anyObject())).andReturn(null);
        replay(service);

        call("authenticate", "{\"username\":\"u\",\"password\":\"hunter2-s3cret\"}", "application/json");

        assertTrue(logs.list.stream().anyMatch(e -> e.getFormattedMessage().contains("authenticate")));
        for (ILoggingEvent event : logs.list) {
            assertFalse(event.getFormattedMessage(), event.getFormattedMessage().contains("hunter2-s3cret"));
        }
    }

    private MvcResult call(String method, String json, String accept) throws Exception {
        MockHttpServletRequestBuilder request = post("/sso.service/" + method)
                .contentType(MediaType.APPLICATION_JSON).header("Accept", accept).content(json);
        return mvc.perform(request).andReturn();
    }
}
