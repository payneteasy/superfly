package com.payneteasy.superfly.web.controller.api;

import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.impl.remote.SSOServiceImpl;
import com.payneteasy.superfly.web.obtainer.AuthenticationPrincipalSubsystemIdentifierObtainer;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * A subsystem authenticated as A must not be able to act as subsystem B through the RPC endpoint.
 */
public class RemoteApiControllerSubsystemHintTest {

    private InternalSSOService internal;
    private MockMvc mvc;

    @Before
    public void setUp() {
        internal = createMock(InternalSSOService.class);
        SSOServiceImpl service = new SSOServiceImpl(internal, createNiceMock(HOTPService.class),
                createNiceMock(ResetPasswordStrategy.class), createNiceMock(EmailService.class),
                createNiceMock(PublicKeyCrypto.class));
        service.setSubsystemIdentifierObtainer(new AuthenticationPrincipalSubsystemIdentifierObtainer());
        mvc = MockMvcBuilders.standaloneSetup(
                new RemoteApiController(service, new ApiSerializationManager())).build();
        SecurityContextHolder.getContext().setAuthentication(
                new com.payneteasy.superfly.web.security.SubsystemAuthenticationToken(
                        "subsystem-a", null, List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM"))));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void foreignSubsystemIdentifierIsRejected() throws Exception {
        replay(internal);

        MvcResult result = call("getUsersWithActions", "{\"subsystemIdentifier\":\"subsystem-b\"}");

        assertAuthError(result);
        verify(internal);
    }

    @Test
    public void foreignSubsystemNameInGetEventsIsRejected() throws Exception {
        replay(internal);

        MvcResult result = call("getEvents", "{\"waitTimeMs\":0,\"subsystemName\":\"subsystem-b\"}");

        assertAuthError(result);
        verify(internal);
    }

    @Test
    public void ownSubsystemIdentifierIsAccepted() throws Exception {
        expect(internal.getUsersWithActions("subsystem-a")).andReturn(Collections.emptyList());
        replay(internal);

        MvcResult result = call("getUsersWithActions", "{\"subsystemIdentifier\":\"subsystem-a\"}");

        assertEquals(200, result.getResponse().getStatus());
        verify(internal);
    }

    @Test
    public void missingSubsystemIdentifierDefaultsToAuthenticatedSubsystem() throws Exception {
        expect(internal.getUsersWithActions("subsystem-a")).andReturn(Collections.emptyList());
        replay(internal);

        assertEquals(200, call("getUsersWithActions", "{}").getResponse().getStatus());
        verify(internal);
    }

    @Test
    public void foreignSubsystemInExplicitRoleGrantIsRejected() throws Exception {
        replay(internal);

        MvcResult result = call("registerUser", registerJson(
                "{\"detectSubsystemIdentifier\":false,\"subsystemIdentifier\":\"subsystem-b\",\"principalName\":\"r\"}"));

        assertAuthError(result);
        verify(internal);
    }

    @Test
    public void ownSubsystemInExplicitRoleGrantIsAccepted() throws Exception {
        expectRegisterUser();

        assertEquals(200, call("registerUser", registerJson(
                "{\"detectSubsystemIdentifier\":false,\"subsystemIdentifier\":\"subsystem-a\",\"principalName\":\"r\"}"))
                .getResponse().getStatus());
        verify(internal);
    }

    @Test
    public void detectedSubsystemInRoleGrantIsAccepted() throws Exception {
        expectRegisterUser();

        assertEquals(200, call("registerUser", registerJson(
                "{\"detectSubsystemIdentifier\":true,\"principalName\":\"r\"}"))
                .getResponse().getStatus());
        verify(internal);
    }

    @Test
    public void lastEventIdIsTakenForTheAuthenticatedSubsystem() throws Exception {
        expect(internal.getLastEventId("subsystem-a")).andReturn(42L);
        replay(internal);

        MvcResult result = call("getLastEventId", "null");

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("42", result.getResponse().getContentAsString());
        verify(internal);
    }

    private void expectRegisterUser() throws Exception {
        internal.registerUser(anyObject(), anyObject(), anyObject(), eq("subsystem-a"), anyObject(),
                anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
        replay(internal);
    }

    private static String registerJson(String grant) {
        return "{\"username\":\"u\",\"password\":\"p\",\"roleGrants\":[" + grant + "]}";
    }

    private MvcResult call(String method, String json) throws Exception {
        return mvc.perform(post("/sso.service/" + method)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json)).andReturn();
    }

    private static void assertAuthError(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertEquals(body, 202, result.getResponse().getStatus());
        assertTrue(body, body.contains("SsoAuthException"));
        assertTrue(body, !body.contains("subsystem-b"));
    }
}
