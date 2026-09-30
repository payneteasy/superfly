package com.payneteasy.superfly.api.client;

import com.payneteasy.http.client.api.*;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.api.request.HasOtpMasterKeyRequest;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.api.serialization.ApiSerializer;
import com.payneteasy.superfly.api.serialization.ExceptionWrapper;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SSOHttpServiceApiClientTest {

    private static final String BASE_URL        = "https://test.example.com/superfly";
    private static final String SUBSYSTEM_NAME  = "test-subsystem";
    private static final String SUBSYSTEM_TOKEN = "test-token";

    private IHttpClient httpClient;

    private ApiSerializationManager serializationManager;
    private SSOHttpServiceApiClient client;

    @Before
    public void setUp() {
        httpClient = EasyMock.createMock(IHttpClient.class);
        serializationManager = new ApiSerializationManager();

        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl(BASE_URL)
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();

        // Inject mock transport directly via new DI constructor
        client = new SSOHttpServiceApiClient(httpClient, config, serializationManager);
    }

    @Test
    public void testConstructorValidation() {
        SSOClientConfig validConfig = SSOClientConfig.builder()
                .baseUrl(BASE_URL)
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();

        // Valid arguments
        new SSOHttpServiceApiClient(httpClient, validConfig, serializationManager);

        // null IHttpClient → NPE
        assertThrows(NullPointerException.class, () ->
                new SSOHttpServiceApiClient(null, validConfig, serializationManager)
        );

        // null SSOClientConfig → NPE
        assertThrows(NullPointerException.class, () ->
                new SSOHttpServiceApiClient(httpClient, null, serializationManager)
        );

        // null ApiSerializationManager → NPE
        assertThrows(NullPointerException.class, () ->
                new SSOHttpServiceApiClient(httpClient, validConfig, null)
        );

        // SSOClientConfig validation: null baseUrl
        assertThrows(IllegalArgumentException.class, () ->
                SSOClientConfig.builder()
                        .baseUrl(null)
                        .subsystemName(SUBSYSTEM_NAME)
                        .defaultParameters(HttpRequestParameters.builder().build())
                        .build()
        );

        // SSOClientConfig validation: empty baseUrl
        assertThrows(IllegalArgumentException.class, () ->
                SSOClientConfig.builder()
                        .baseUrl("")
                        .subsystemName(SUBSYSTEM_NAME)
                        .defaultParameters(HttpRequestParameters.builder().build())
                        .build()
        );

        // SSOClientConfig validation: null subsystemName
        assertThrows(NullPointerException.class, () ->
                SSOClientConfig.builder()
                        .baseUrl(BASE_URL)
                        .subsystemName(null)
                        .defaultParameters(HttpRequestParameters.builder().build())
                        .build()
        );

        // URL without trailing / handled correctly
        SSOClientConfig cfg1 = SSOClientConfig.builder()
                .baseUrl("https://example.com/api")
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();
        assertEquals("https://example.com/api", cfg1.getBaseUrl());

        // URL with trailing / is trimmed
        SSOClientConfig cfg2 = SSOClientConfig.builder()
                .baseUrl("https://example.com/api/")
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();
        assertEquals("https://example.com/api", cfg2.getBaseUrl());
    }


    @Test
    public void testAuthenticate_Success() throws SsoAuthException, HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request      = new AuthenticateRequest("user", "password", null);
        SSOUser             expectedUser = createTestSSOUser();

        // Configure mock to return a successful response
        HttpResponse response = createSuccessResponse(
                serializationManager.serialize(expectedUser)
        );
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Call the method under test
        SSOUser result = client.authenticate(request);

        // Verify the result
        assertNotNull(result);
        assertEquals(expectedUser.getName(), result.getName());

        // Verify that the HTTP request was correctly formed
        verify(httpClient);

        // Create a new capture for the second call
        Capture<HttpRequest> requestCapture = newCapture();
        reset(httpClient);

        expect(httpClient.send(capture(requestCapture), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Call the method again to capture the request
        client.authenticate(request);

        HttpRequest capturedRequest = requestCapture.getValue();
        assertEquals(BASE_URL + "/authenticate", capturedRequest.getUrl());
        assertEquals(HttpMethod.POST, capturedRequest.getMethod());

        // Verify headers
        assertHeaderContains(capturedRequest, "X-Subsystem-Name", SUBSYSTEM_NAME);
        assertHeaderContains(capturedRequest, "X-Subsystem-Token", SUBSYSTEM_TOKEN);
        assertHeaderContains(capturedRequest, "Content-Type", ApiSerializer.CONTENT_TYPE_JSON);
        assertHeaderContains(capturedRequest, "Accept", ApiSerializer.CONTENT_TYPE_JSON);
    }

    @Test
    public void testCheckOtp_Success() throws SsoAuthException, HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        SSOUser         user    = createTestSSOUser();
        CheckOtpRequest request = new CheckOtpRequest(user, "123456");

        // Create a Capture object to capture the argument
        Capture<HttpRequest> requestCapture = newCapture();

        // Success response
        HttpResponse response = createSuccessResponse("true");
        expect(httpClient.send(capture(requestCapture), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Call the method under test
        boolean result = client.checkOtp(request);

        // Verify the result
        assertTrue(result);

        // Verify that the HTTP request was correctly formed
        verify(httpClient);
        HttpRequest capturedRequest = requestCapture.getValue();
        assertEquals(BASE_URL + "/checkOtp", capturedRequest.getUrl());
    }

    @Test
    public void testHasOtpMasterKey_Success() throws SsoAuthException, HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        HasOtpMasterKeyRequest request = new HasOtpMasterKeyRequest("username");

        // Create a Capture object to capture the argument
        Capture<HttpRequest> requestCapture = newCapture();

        // Configure mock to return a successful response
        HttpResponse response = createSuccessResponse("true");
        expect(httpClient.send(capture(requestCapture), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Call the method under test
        boolean result = client.hasOtpMasterKey(request);

        // Verify the result
        assertTrue(result);

        // Verify HTTP request
        verify(httpClient);
        HttpRequest capturedRequest = requestCapture.getValue();
        assertEquals(BASE_URL + "/hasOtpMasterKey", capturedRequest.getUrl());
    }

    @Test
    public void testHandleConnectionException() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password", null);

        // Создаем исключение заранее с конкретным значением для cause
        Exception            cause            = new Exception("Connection error");
        HttpConnectException connectException = new HttpConnectException("Connection refused", cause);

        // Configure mock to simulate a connection error
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andThrow(connectException);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoConnectionException.class, () -> client.authenticate(request));
    }

    @Test
    public void testHandleReadException() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password", null);

        Exception         cause                = new Exception("Read timeout error");
        HttpReadException readTimeoutException = new HttpReadException("Read timeout", cause);

        // Configure mock to simulate a read error
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andThrow(readTimeoutException);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoConnectionException.class, () -> client.authenticate(request));
    }

    @Test
    public void testHandleServerErrors() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password");

        // 500 Internal Server Error
        HttpResponse serverErrorResponse = createErrorResponse(500, "Internal Server Error");
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(serverErrorResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoServerException.class, () -> client.authenticate(request));
    }

    @Test
    public void testHandleClientErrors() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password");

        // 400 Bad Request
        HttpResponse badRequestResponse = createErrorResponse(400, "Bad Request");
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(badRequestResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoBadRequestException.class, () -> client.authenticate(request));

        // 401 Unauthorized
        HttpResponse unauthorizedResponse = createErrorResponse(401, "Unauthorized");
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(unauthorizedResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoUnauthorizedException.class, () -> client.authenticate(request));

        // 403 Forbidden
        HttpResponse forbiddenResponse = createErrorResponse(403, "Forbidden");
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(forbiddenResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoForbiddenException.class, () -> client.authenticate(request));

        // 404 Not Found
        HttpResponse notFoundResponse = createErrorResponse(404, "Not Found");
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(notFoundResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoNotFoundException.class, () -> client.authenticate(request));

        // 409 Conflict
        HttpResponse conflictResponse = createErrorResponse(409, "Conflict");
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(conflictResponse);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoConflictException.class, () -> client.authenticate(request));
    }

    @Test
    public void testExceptionWrapperInResponse() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Create ExceptionWrapper for UserExistsException
        ExceptionWrapper wrapper = new ExceptionWrapper(
                UserExistsException.class.getName(),
                "User already exists",
                "UserExistsException: User already exists"
        );

        // Serialize ExceptionWrapper
        String serializedWrapper = serializationManager.serialize(wrapper);

        // Create response with ExceptionWrapper
        HttpResponse response = createErrorResponse(202, serializedWrapper);

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that when receiving a serialized ExceptionWrapper
        // the client recreates and throws the correct exception
        try {
            client.registerUser(new UserRegisterRequest());
            fail("Expected UserExistsException");
        } catch (UserExistsException e) {
            // Expected exception
            assertEquals("User already exists", e.getMessage());
        } catch (Exception e) {
            fail("Expected UserExistsException but got " + e.getClass().getName());
        }
    }

    @Test
    public void testExceptionWrapperWithUnknownExceptionClass() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Create ExceptionWrapper with an unknown exception class
        PolicyValidationException policyValidationException = new PolicyValidationException(PolicyValidationException.SHORT_PASSWORD);
        ExceptionWrapper wrapper = ExceptionWrapper.from(policyValidationException);

        // Serialize ExceptionWrapper
        String serializedWrapper = serializationManager.serialize(wrapper);

        // Create a response with serialized ExceptionWrapper
        HttpResponse response = createErrorResponse(202, serializedWrapper);

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that when receiving an ExceptionWrapper with an unknown class
        // the client will create an SsoException with the appropriate message
        try {
            client.registerUser(new UserRegisterRequest());
            fail("Expected SsoException");
        } catch (SsoException e) {
            // Expected exception
            assertTrue(e instanceof PolicyValidationException);
            assertTrue(e.getMessage().contains("P001"));
        } catch (Exception e) {
            fail("Expected SsoException but got " + e.getClass().getName());
        }
    }

    @Test
    public void testExceptionWrapperWithNullValues() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Create ExceptionWrapper with null in the message field
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.exceptions.UserExistsException",
                null,
                "UserExistsException: null message"
        );

        // Serialize ExceptionWrapper
        String serializedWrapper = serializationManager.serialize(wrapper);

        // Создаем ответ с сериализованным ExceptionWrapper
        HttpResponse response = createErrorResponse(202, serializedWrapper);

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that when receiving an ExceptionWrapper with null in the message field
        // the client will still correctly process the exception
        try {
            client.registerUser(new UserRegisterRequest());
            fail("Expected UserExistsException");
        } catch (UserExistsException e) {
            // Expected exception
            assertNull(e.getMessage());
        } catch (Exception e) {
            fail("Expected UserExistsException but got " + e.getClass().getName());
        }
    }

    @Test
    public void testValidContentButNotExceptionWrapper() throws HttpWriteException, HttpConnectException, HttpReadException {
        String validJson = "{}";

        // Create a response with valid JSON that is not an ExceptionWrapper
        HttpResponse response = createSuccessResponse(validJson);

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // In this case, the client should try to deserialize the response as the expected data type
        // not as an ExceptionWrapper, and if it fails, throw an SsoParseException
        try {
            SSOUser authenticate = client.authenticate(new AuthenticateRequest("user", "password"));
            assertNotNull(authenticate);
            assertNull(authenticate.getName());
        } catch (SsoParseException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Failed to parse"));
        }
    }

    @Test
    public void testNonStandardContentType() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Create ExceptionWrapper
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.exceptions.UserExistsException",
                "User already exists",
                "UserExistsException: User already exists"
        );

        // Serialize ExceptionWrapper
        String serializedWrapper = serializationManager.serialize(wrapper);

        // Custom Content-Type
        List<HttpHeader> customHeaders = List.of(
                new HttpHeader("Content-Type", "application/custom+json")
        );

        // Create a response with serialized ExceptionWrapper and non-standard Content-Type
        HttpResponse response = new HttpResponse(
                200, "OK", customHeaders, serializedWrapper.getBytes(StandardCharsets.UTF_8)
        );

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that the client correctly handles non-standard Content-Type
        // Depending on the implementation of ApiSerializationManager, this test may pass
        // or throw an SsoParseException if the serializer doesn't support this Content-Type
        try {
            client.registerUser(new UserRegisterRequest());
            // If the client found a suitable serializer for Content-Type
            fail("Expected UserExistsException or SsoParseException");
        } catch (UserExistsException e) {
            // Expected exception if the client found a suitable serializer
            assertEquals("User already exists", e.getMessage());
        } catch (SsoParseException e) {
            // Or expected exception if the client did not find a suitable serializer
            assertTrue(e.getMessage().contains("Failed to parse"));
        } catch (Exception e) {
            fail("Expected UserExistsException or SsoParseException but got " + e.getClass().getName());
        }
    }

    @Test
    public void testParseResponse_InvalidJson() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password");

        // Настройка мока для возврата ответа с невалидным JSON
        HttpResponse response = createSuccessResponse("not a valid json");
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that the client method throws the expected exception
        assertThrows(SsoParseException.class, () -> client.authenticate(request));
    }

    @Test
    public void testParseInvalidExceptionWrapper() throws HttpWriteException, HttpConnectException, HttpReadException {
        // Prepare test data
        AuthenticateRequest request = new AuthenticateRequest("user", "password");

        // Создаем ответ с сериализованным ExceptionWrapper
        HttpResponse response = createSuccessResponse( "{");

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        // Verify that the client handles this case correctly (should not throw an exception)
        try {
            client.authenticate(request);
            // We expect that in this case the client will try to deserialize the response as SSOUser
            // and throw an SsoParseException because the format doesn't match
            fail("Expected SsoParseException to be thrown");
        } catch (SsoParseException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Failed to parse"));
        } catch (Exception e) {
            fail("Expected SsoParseException but got " + e.getClass().getName());
        }
    }

    // ── new tests: per-endpoint timeouts, AutoCloseable, ExceptionWrapper for 4xx/5xx ──

    @Test
    public void testPerEndpointTimeoutOverride() throws Exception {
        HttpRequestParameters defaultParams = HttpRequestParameters.builder()
                .timeouts(new com.payneteasy.http.client.api.HttpTimeouts(5_000, 30_000)).build();
        HttpRequestParameters eventsParams  = HttpRequestParameters.builder()
                .timeouts(new com.payneteasy.http.client.api.HttpTimeouts(5_000, 90_000)).build();
        HttpRequestParameters authParams    = HttpRequestParameters.builder()
                .timeouts(new com.payneteasy.http.client.api.HttpTimeouts(5_000, 10_000)).build();

        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl(BASE_URL)
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(defaultParams)
                .endpointParameter(Endpoint.GET_EVENTS,   eventsParams)
                .endpointParameter(Endpoint.AUTHENTICATE, authParams)
                .build();

        SSOHttpServiceApiClient perEndpointClient =
                new SSOHttpServiceApiClient(httpClient, config, serializationManager);

        Capture<HttpRequestParameters> paramsCapture = newCapture();
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), capture(paramsCapture)))
                .andReturn(createSuccessResponse("[]"));
        replay(httpClient);

        perEndpointClient.getEvents(com.payneteasy.superfly.api.request.GetEventsRequest.builder().build());

        assertSame("GET_EVENTS must use eventsParams", eventsParams, paramsCapture.getValue());
        verify(httpClient);

        // Now AUTHENTICATE should use authParams
        Capture<HttpRequestParameters> authCapture = newCapture();
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), capture(authCapture)))
                .andReturn(createSuccessResponse(serializationManager.serialize(createTestSSOUser())));
        replay(httpClient);

        perEndpointClient.authenticate(new AuthenticateRequest("user", "pass"));

        assertSame("AUTHENTICATE must use authParams", authParams, authCapture.getValue());
        verify(httpClient);

        // sendSystemData has no override → defaultParams
        Capture<HttpRequestParameters> defaultCapture = newCapture();
        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), capture(defaultCapture)))
                .andReturn(createSuccessResponse("null"));
        replay(httpClient);

        perEndpointClient.sendSystemData(
                com.payneteasy.superfly.api.request.SendSystemDataRequest.builder().build());

        assertSame("SEND_SYSTEM_DATA must fall back to defaultParams", defaultParams, defaultCapture.getValue());
        verify(httpClient);
    }

    @Test
    public void testAutoCloseable_DelegatesToTransport() throws Exception {
        CloseableHttpClient closeableTransport = createMock(CloseableHttpClient.class);
        closeableTransport.close();
        expectLastCall().once();
        replay(closeableTransport);

        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl(BASE_URL).subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();

        try (SSOHttpServiceApiClient c =
                     new SSOHttpServiceApiClient(closeableTransport, config, serializationManager)) {
            // use try-with-resources
            assertNotNull(c);
        }

        verify(closeableTransport);
    }

    @Test
    public void testAutoCloseable_NonCloseableTransport() throws Exception {
        // httpClient mock is NOT AutoCloseable — close() should be a no-op
        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl(BASE_URL).subsystemName(SUBSYSTEM_NAME)
                .subsystemToken(SUBSYSTEM_TOKEN)
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();

        try (SSOHttpServiceApiClient c =
                     new SSOHttpServiceApiClient(httpClient, config, serializationManager)) {
            assertNotNull(c);
        }
        // No verify needed — just ensure close() doesn't throw on non-AutoCloseable transport
    }

    @Test
    public void testExceptionWrapperFor400() throws Exception {
        // Server sends ExceptionWrapper with status 400 (real HTTP error semantics).
        // Bug fix: previously the client threw generic SsoBadRequestException; now it
        // recovers the typed UserExistsException.
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.exceptions.UserExistsException",
                "User foo already exists",
                "UserExistsException: User foo already exists"
        );
        HttpResponse response = new HttpResponse(
                400, "Bad Request",
                List.of(new HttpHeader("Content-Type", ApiSerializer.CONTENT_TYPE_JSON)),
                serializationManager.serialize(wrapper).getBytes(StandardCharsets.UTF_8)
        );

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        try {
            client.registerUser(new UserRegisterRequest());
            fail("Expected UserExistsException");
        } catch (UserExistsException e) {
            assertEquals("User foo already exists", e.getMessage());
        }
    }

    @Test
    public void testExceptionWrapperFor500() throws Exception {
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.exceptions.SsoSystemException",
                "DB unavailable",
                "SsoSystemException: DB unavailable"
        );
        HttpResponse response = new HttpResponse(
                500, "Internal Server Error",
                List.of(new HttpHeader("Content-Type", ApiSerializer.CONTENT_TYPE_JSON)),
                serializationManager.serialize(wrapper).getBytes(StandardCharsets.UTF_8)
        );

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        try {
            client.sendSystemData(
                    com.payneteasy.superfly.api.request.SendSystemDataRequest.builder().build());
            fail("Expected SsoSystemException (typed from wrapper, not generic SsoServerException)");
        } catch (com.payneteasy.superfly.api.exceptions.SsoSystemException e) {
            assertEquals("DB unavailable", e.getMessage());
        }
    }

    @Test
    public void testFallbackOn400WithoutWrapper() throws Exception {
        // Plain-text body that is NOT a valid ExceptionWrapper JSON — fallback to status-based exception
        HttpResponse response = createErrorResponse(400, "Bad Request from gateway");

        reset(httpClient);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(response);
        replay(httpClient);

        try {
            client.authenticate(new AuthenticateRequest("user", "pass"));
            fail("Expected SsoBadRequestException (fallback)");
        } catch (SsoBadRequestException e) {
            assertTrue(e.getMessage().contains("400"));
        }
    }

    /** Mock-friendly interface combining IHttpClient + AutoCloseable for AutoCloseable delegation tests. */
    private interface CloseableHttpClient extends IHttpClient, AutoCloseable {
    }

    // Helper methods

    private SSOUser createTestSSOUser() {
        SSOAction action = new SSOAction("test_action", true);
        SSORole   role   = new SSORole("test_role");

        Map<SSORole, SSOAction[]> actionsMap = new HashMap<>();
        actionsMap.put(role, new SSOAction[]{action});

        SSOUser user = new SSOUser("test_user", actionsMap, Collections.emptyMap());
        user.setSessionId("test_session");
        user.setOtpType(OTPType.GOOGLE_AUTH);
        user.setOtpOptional(true);

        return user;
    }

    private HttpResponse createSuccessResponse(String body) {
        List<HttpHeader> httpHeaders = List.of(
                new HttpHeader("Content-Type", ApiSerializer.CONTENT_TYPE_JSON)
        );
        return new HttpResponse(200, "OK", httpHeaders, body.getBytes(StandardCharsets.UTF_8));
    }

    private HttpResponse createErrorResponse(int statusCode, String message) {
        List<HttpHeader> httpHeaders = List.of(
                new HttpHeader("Content-Type", "text/plain")
        );

        return new HttpResponse(statusCode, "Error", httpHeaders, message.getBytes(StandardCharsets.UTF_8));
    }

    private void assertHeaderContains(HttpRequest request, String headerName, String expectedValue) {
        boolean found = request.getHeaders()
                               .asList()
                               .stream()
                               .anyMatch(header ->
                                                 header.getName().equals(headerName)
                                                         && header.getValue().equals(expectedValue)
                               )
                ;
        assertTrue("Header " + headerName + " with value " + expectedValue + " not found", found);
    }

    private void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expectedType.getSimpleName() + " to be thrown");
        } catch (Throwable t) {
            if (!expectedType.isInstance(t)) {
                fail("Expected " + expectedType.getSimpleName() + " but got " + t.getClass().getSimpleName());
            }
            // Reset the state of mocks after exceptions
            resetAll();
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void resetAll() {
        reset(httpClient);
    }
}
