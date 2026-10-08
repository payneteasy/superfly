package com.payneteasy.superfly.web.mvc;

import com.payneteasy.superfly.service.RemoteAuthService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthSession;
import com.payneteasy.superfly.web.controller.api.GlobalExceptionHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Настоящий контекст сервлета {@code rest-api} ({@code /sso/check/*}): dispatcher-servlet.xml с
 * detectAllHandlerMappings=false, родитель — root-контекст с {@link GlobalExceptionHandler}.
 * Фиксирует контракт ответов remote-auth (статусы, type, title/detail) и 404/JSON для неизвестных путей.
 */
public class RestApiServletContextTest {

    private static final String SERVLET_PATH = "/sso/check";
    private static final String PASSWORD_BODY = "{\"username\":\"admin\",\"passwordEncrypted\":\"enc\"}";
    private static final String OTP_BODY = "{\"username\":\"admin\",\"otpEncrypted\":\"enc\",\"sessionToken\":\"tok\"}";

    private static AnnotationConfigWebApplicationContext root;
    private static DispatcherServlet servlet;

    @BeforeClass
    public static void setUp() throws Exception {
        RemoteAuthService service = createNiceMock(RemoteAuthService.class);
        expect(service.checkPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .andAnswer(() -> {
                    String subsystem = (String) org.easymock.EasyMock.getCurrentArguments()[0];
                    if ("fail".equals(subsystem)) {
                        throw new RemoteAuthException("boom", "INTERNAL_ERROR");
                    }
                    if ("denied".equals(subsystem)) {
                        throw new RemoteAuthException("nope", "UNAUTHORIZED");
                    }
                    if ("invalid".equals(subsystem)) {
                        throw new RemoteAuthException("bad creds", "INVALID_CREDENTIALS");
                    }
                    return new RemoteAuthSession("session-1", true);
                }).anyTimes();
        expect(service.checkOtp(anyString(), anyString(), anyString(), anyString(), anyString()))
                .andReturn("OK").anyTimes();
        replay(service);

        MockServletContext servletContext = new MockServletContext("file:src/main/webapp", new FileSystemResourceLoader());
        root = new AnnotationConfigWebApplicationContext();
        root.setServletContext(servletContext);
        root.register(GlobalExceptionHandler.class);
        root.addBeanFactoryPostProcessor(bf -> bf.registerSingleton("remoteAuthService", service));
        root.refresh();

        MockServletConfig config = new MockServletConfig(servletContext, "rest-api");
        config.addInitParameter("contextConfigLocation", "/WEB-INF/spring/dispatcher-servlet.xml");
        config.addInitParameter("detectAllHandlerMappings", "false");
        servletContext.setAttribute(
                org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, root);
        servlet = new DispatcherServlet();
        servlet.init(config);
    }

    @AfterClass
    public static void tearDown() {
        servlet.destroy();
        root.close();
    }

    @Test
    public void passwordOk() throws Exception {
        MockHttpServletResponse r = post("/check-password/billing/admin", PASSWORD_BODY, "Bearer t", "application/json");
        assertEquals(200, r.getStatus());
        assertTrue(r.getContentAsString().contains("\"sessionToken\":\"session-1\""));
    }

    @Test
    public void otpOk() throws Exception {
        assertEquals(200, post("/check-otp/billing/admin", OTP_BODY, "Bearer t", "application/json").getStatus());
    }

    @Test
    public void missingAuthorizationIs401() throws Exception {
        assertError(post("/check-password/billing/admin", PASSWORD_BODY, null, "application/json"),
                401, "UNAUTHORIZED", "Missing or invalid Authorization header");
        assertError(post("/check-otp/billing/admin", OTP_BODY, null, "application/json"),
                401, "UNAUTHORIZED", "Missing or invalid Authorization header");
    }

    @Test
    public void missingFieldsIs400() throws Exception {
        assertError(post("/check-password/billing/admin", "{\"username\":\"admin\"}", "Bearer t", "application/json"),
                400, "BAD_REQUEST", "Missing passwordEncrypted field");
        assertError(post("/check-otp/billing/admin", "{\"username\":\"admin\"}", "Bearer t", "application/json"),
                400, "BAD_REQUEST", "Missing otpEncrypted or sessionToken field");
    }

    @Test
    public void usernameMismatchIs400() throws Exception {
        assertError(post("/check-password/billing/other", PASSWORD_BODY, "Bearer t", "application/json"),
                400, "BAD_REQUEST", "Username in path and body must match");
        assertError(post("/check-otp/billing/other", OTP_BODY, "Bearer t", "application/json"),
                400, "BAD_REQUEST", "Username in path and body must match");
    }

    @Test
    public void invalidJsonIs400() throws Exception {
        assertError(post("/check-password/billing/admin", "{not json", "Bearer t", "application/json"),
                400, "BAD_REQUEST", "Invalid JSON");
    }

    @Test
    public void remoteAuthExceptionsMapToStatuses() throws Exception {
        assertError(post("/check-password/fail/admin", PASSWORD_BODY, "Bearer t", "application/json"),
                500, "INTERNAL_ERROR", "boom");
        assertError(post("/check-password/denied/admin", PASSWORD_BODY, "Bearer t", "application/json"),
                401, "UNAUTHORIZED", "nope");
        assertError(post("/check-password/invalid/admin", PASSWORD_BODY, "Bearer t", "application/json"),
                400, "INVALID_CREDENTIALS", "bad creds");
    }

    @Test
    public void unknownRouteIs404Json() throws Exception {
        for (String accept : new String[]{null, "*/*", "application/json"}) {
            MockHttpServletResponse r = post("/check-password", "{}", "Bearer t", accept);
            assertEquals("accept=" + accept, 404, r.getStatus());
            assertTrue(r.getContentType(), r.getContentType().startsWith("application/json"));
            assertFalse(r.getContentAsString().contains("No endpoint"));
            assertTrue(r.getContentAsString().contains("\"type\":\"NOT_FOUND\""));
        }
    }

    @Test
    public void errorsAreJsonWithoutAcceptAndWithWildcard() throws Exception {
        for (String accept : new String[]{null, "*/*"}) {
            MockHttpServletResponse r = post("/check-password/billing/admin", PASSWORD_BODY, null, accept);
            assertEquals(401, r.getStatus());
            assertTrue(r.getContentType(), r.getContentType().startsWith("application/json"));
            assertTrue(r.getContentAsString().startsWith("{"));
        }
    }

    // produces=application/json не совпадает с явным Accept: application/xml -> 406 внутри Spring,
    // который GlobalExceptionHandler отдаёт как 500; поведение фиксируем как есть.
    @Test
    public void explicitXmlAcceptBehaviourIsUnchanged() throws Exception {
        assertEquals(500, post("/check-password/billing/admin", PASSWORD_BODY, null, "application/xml").getStatus());
    }

    private static void assertError(MockHttpServletResponse r, int status, String type, String message) throws Exception {
        assertEquals(status, r.getStatus());
        String body = r.getContentAsString();
        assertTrue(body, body.contains("\"type\":\"" + type + "\""));
        assertTrue(body, body.contains("\"title\":\"" + message + "\""));
        assertTrue(body, body.contains("\"detail\":\"" + message + "\""));
        assertEquals("en", r.getHeader("Content-Language"));
    }

    private static MockHttpServletResponse post(String pathInfo, String body, String auth, String accept) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(root.getServletContext(), "POST", SERVLET_PATH + pathInfo);
        request.setServletPath(SERVLET_PATH);
        request.setPathInfo(pathInfo);
        request.setContentType("application/json");
        request.setContent(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (auth != null) {
            request.addHeader("Authorization", auth);
        }
        if (accept != null) {
            request.addHeader("Accept", accept);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        return response;
    }
}
