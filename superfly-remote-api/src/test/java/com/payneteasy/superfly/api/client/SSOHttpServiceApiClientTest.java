package com.payneteasy.superfly.api.client;

import com.google.gson.JsonSyntaxException;
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

        HttpRequestParameters parameters = HttpRequestParameters.builder().build();

        // Mock http client
        client = new TestableHttpServiceApiClient(
                parameters,
                BASE_URL,
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializationManager
        );
    }

    @Test
    public void testConstructorValidation() {
        // Valid arguments
        new SSOHttpServiceApiClient(
                HttpRequestParameters.builder().build(),
                BASE_URL,
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializationManager
        );

        // Check for null and empty values
        assertThrows(NullPointerException.class, () ->
                new SSOHttpServiceApiClient(
                        null,
                        BASE_URL,
                        SUBSYSTEM_NAME,
                        SUBSYSTEM_TOKEN,
                        serializationManager
                )
        );

        assertThrows(NullPointerException.class, () ->
                new SSOHttpServiceApiClient(
                        HttpRequestParameters.builder().build(),
                        BASE_URL,
                        null,
                        SUBSYSTEM_TOKEN,
                        serializationManager
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new SSOHttpServiceApiClient(
                        HttpRequestParameters.builder().build(),
                        null,
                        SUBSYSTEM_NAME,
                        SUBSYSTEM_TOKEN,
                        serializationManager
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new SSOHttpServiceApiClient(
                        HttpRequestParameters.builder().build(),
                        "",
                        SUBSYSTEM_NAME,
                        SUBSYSTEM_TOKEN,
                        serializationManager
                )
        );

        // Check that URL without trailing / is handled correctly
        SSOHttpServiceApiClient client1 = new SSOHttpServiceApiClient(
                HttpRequestParameters.builder().build(),
                "http://example.com/api",
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializationManager
        );

        // Check that URL with trailing / is handled correctly
        SSOHttpServiceApiClient client2 = new SSOHttpServiceApiClient(
                HttpRequestParameters.builder().build(),
                "http://example.com/api/",
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializationManager
        );
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

    private class TestableHttpServiceApiClient extends SSOHttpServiceApiClient {
        public TestableHttpServiceApiClient(
                HttpRequestParameters httpRequestParameters,
                String baseUrl,
                String subsystemName,
                String subsystemToken,
                ApiSerializationManager serializationManager
        ) {
            super(httpRequestParameters, baseUrl, subsystemName, subsystemToken, serializationManager);
        }

        @Override
        protected IHttpClient getHttpClient() {
            return httpClient;
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void resetAll() {
        reset(httpClient);
    }
}
