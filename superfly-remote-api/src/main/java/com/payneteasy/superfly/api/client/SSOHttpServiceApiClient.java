package com.payneteasy.superfly.api.client;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.payneteasy.http.client.api.*;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.http.client.impl.HttpClientImpl;
import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.api.serialization.ExceptionSerializationHelper;
import com.payneteasy.superfly.api.serialization.ExceptionWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.payneteasy.superfly.api.serialization.ApiSerializer.CONTENT_TYPE_JSON;

@Slf4j
public class SSOHttpServiceApiClient implements SSOService {

    private static final String HEADER_SUBSYSTEM_NAME  = "X-Subsystem-Name";
    private static final String HEADER_SUBSYSTEM_TOKEN = "X-Subsystem-Token";
    private static final String HEADER_CONTENT_TYPE    = "Content-Type";
    private static final String HEADER_ACCEPT          = "Accept";

    private final IHttpClient             httpClient;
    private final HttpRequestParameters   parameters;
    private final String                  baseUrl;
    private final String                  subsystemName;
    private final String                  subsystemToken;
    private final ApiSerializationManager serializationManager;

    /**
     * @param subsystemToken can be null if use x509 authentication
     * @param baseUrl        for example https://superfly.payneteasy.com/superfly/remoting/sso.service
     */
    public SSOHttpServiceApiClient(
            HttpRequestParameters httpRequestParameters,
            String baseUrl,
            String subsystemName,
            @Nullable String subsystemToken,
            ApiSerializationManager serializationManager
    ) {
        this.httpClient = getHttpClient();

        this.parameters = Objects.requireNonNull(httpRequestParameters, "httpRequestParameters must not be null");
        this.baseUrl = validateUrl(baseUrl);
        this.subsystemName = Objects.requireNonNull(subsystemName, "subsystemName must not be null");
        this.subsystemToken = subsystemToken;
        this.serializationManager = serializationManager;
    }

    protected IHttpClient getHttpClient() {
        return new HttpClientImpl();
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    // Interface method implementations

    @Override
    public SSOUser authenticate(AuthenticateRequest request) throws SsoAuthException {
        return post("/authenticate", request, SSOUser.class);
    }

    @Override
    public boolean checkOtp(CheckOtpRequest request) throws SsoAuthException {
        return post("/checkOtp", request, Boolean.class);
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) throws SsoAuthException {
        return post("/hasOtpMasterKey", request, Boolean.class);
    }

    @Override
    public SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request) throws SsoAuthException {
        return post("/hasOtpMasterKey", request, SSOUser.class);
    }

    @Override
    public void sendSystemData(SendSystemDataRequest request) throws SsoSystemException {
        post("/sendSystemData", request, Void.class);
    }

    @Override
    public List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request) throws SsoDataException {
        return post(
                "/getUsersWithActions",
                request,
                new TypeToken<>() {
                }
        );
    }

    @Override
    public void updateUserOtpType(UpdateUserOtpTypeRequest request) throws SsoUserException {
        post("/updateUserOtpType", request, Void.class);
    }

    @Override
    public void registerUser(UserRegisterRequest request) throws UserExistsException, PolicyValidationException,
            BadPublicKeyException, MessageSendException {
        // Simply pass the request, exceptions will be handled automatically
        // through the ExceptionWrapper mechanism
        post("/registerUser", request, Void.class);
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        post("/changeTempPassword", request, Void.class);
    }

    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) throws SsoUserException {
        return post("/getUserDescription", request, UserDescription.class);
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        return post("/resetGoogleAuthMasterKey", request, String.class);
    }

    @Override
    public String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request) {
        return post("/getUrlToGoogleAuthQrCode", request, String.class);
    }

    @Override
    public void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request) throws SsoUserException {
        post("/updateUserIsOtpOptionalValue", request, Void.class);
    }

    @Override
    public void updateUserDescription(UpdateUserDescriptionRequest request)
            throws UserNotFoundException, BadPublicKeyException {
        post("/updateUserDescription", request, Void.class);
    }

    @Override
    public void resetPassword(PasswordResetRequest reset) throws UserNotFoundException, PolicyValidationException {
        post("/resetPassword", reset, Void.class);
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) throws SsoDataException {
        return post(
                "/getUserStatuses",
                request,
                new TypeToken<>() {
                }
        );
    }

    @Override
    public SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request) throws SsoAuthException {
        return post("/exchangeSubsystemToken", request, SSOUser.class);
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) throws SsoSystemException {
        post("/touchSessions", request, Void.class);
    }

    @Override
    public void completeUser(CompleteUserRequest request) throws SsoUserException {
        post("/completeUser", request, Void.class);
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) throws SsoUserException {
        post("/changeUserRole", request, Void.class);
    }

    private <T> T post(String endpoint, Object request, Class<T> responseClass) throws SsoClientException {
        return post(endpoint, request, TypeToken.get(responseClass));
    }

    private <T> T post(String endpoint, Object request, TypeToken<T> typeToken) throws SsoClientException {
        String url         = baseUrl + endpoint;
        String requestBody = serializationManager.serialize(request);

        if (log.isDebugEnabled()) {
            log.debug("Sending request to {}: {}", url, requestBody);
        }

        HttpResponse response;
        try {
            response = httpClient.send(
                    buildRequest(url, requestBody),
                    parameters
            );
            if (log.isDebugEnabled()) {
                log.debug("Get response status: {}, body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (HttpConnectException | HttpWriteException | HttpReadException e) {
            throw new SsoConnectionException("Connection error: " + e.getMessage(), e);
        }

        validateResponse(response, url);
        return parseResponse(response, typeToken.getType());
    }

    private HttpRequest buildRequest(String url, String body) {
        return HttpRequest.builder()
                          .url(url)
                          .method(HttpMethod.POST)
                          .headers(createHeaders())
                          .body(body.getBytes(StandardCharsets.UTF_8))
                          .build();
    }

    private HttpHeaders createHeaders() {
        List<HttpHeader> headers = new ArrayList<>(List.of(
                new HttpHeader(HEADER_SUBSYSTEM_NAME, subsystemName),
                new HttpHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON),
                new HttpHeader(HEADER_ACCEPT, CONTENT_TYPE_JSON)
        ));

        if (subsystemToken != null) {
            headers.add(new HttpHeader(HEADER_SUBSYSTEM_TOKEN, subsystemToken));
        }

        return new HttpHeaders(headers);
    }

    private void validateResponse(HttpResponse response, String url) throws SsoClientException {
        int statusCode = response.getStatusCode();

        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        String body    = new String(response.getBody(), StandardCharsets.UTF_8);
        String message = String.format("HTTP error %d from %s: %s", statusCode, url, body);

        switch (statusCode) {
            case 400:
                throw new SsoBadRequestException(message);
            case 401:
                throw new SsoUnauthorizedException(message);
            case 403:
                throw new SsoForbiddenException(message);
            case 404:
                throw new SsoNotFoundException(message);
            case 409:
                throw new SsoConflictException(message);
            default:
                if (statusCode >= 500) {
                    throw new SsoServerException(message);
                }
                throw new SsoClientException(statusCode, message);
        }
    }

    private <T> T parseResponse(HttpResponse response, Type type) throws SsoParseException {
        String responseText = new String(response.getBody(), StandardCharsets.UTF_8);
        String contentType = response
                .getHeaders()
                .stream()
                .filter(s -> s.getName().equalsIgnoreCase(HEADER_CONTENT_TYPE))
                .findFirst()
                .orElseGet(() -> new HttpHeader(HEADER_CONTENT_TYPE, serializationManager.getDefaultContentType()))
                .getValue()
                ;


        if (response.getStatusCode() != 200) {
            ExceptionWrapper exceptionWrapper;
            try {
                exceptionWrapper = (ExceptionWrapper) serializationManager.deserialize(
                        responseText, ExceptionWrapper.class, contentType);
            } catch (JsonSyntaxException e) {
                throw new SsoParseException("Failed to parse response: " + e.getMessage(), e);
            }

            if (exceptionWrapper != null && exceptionWrapper.getExceptionClass() != null) {
                // Recreate the exception on the client side
                Throwable exception = ExceptionSerializationHelper.createException(exceptionWrapper);
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new SsoParseException("Received exception: " + exception.getMessage(), exception);
                }
            }
        }
        try {
            // Check if the response contains exception information
            @SuppressWarnings("unchecked")
            T result = (T) serializationManager.deserialize(responseText, type, contentType);
            return result;
        } catch (SsoException e) {
            // Propagate SsoException exceptions further
            throw e;
        } catch (Exception e) {
            throw new SsoParseException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    // handleSpecificExceptions removed, as the ExceptionWrapper mechanism is now used

}
