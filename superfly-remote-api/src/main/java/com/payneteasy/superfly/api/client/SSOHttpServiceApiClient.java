package com.payneteasy.superfly.api.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.payneteasy.http.client.api.*;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SSOHttpServiceApiClient implements SSOService {

    private static final String CONTENT_TYPE_JSON      = "application/json";
    private static final String HEADER_SUBSYSTEM_NAME  = "X-Subsystem-Name";
    private static final String HEADER_SUBSYSTEM_TOKEN = "X-Subsystem-Token";
    private static final String HEADER_HOST            = "Host";
    private static final String HEADER_CONTENT_TYPE    = "Content-Type";

    private final IHttpClient           httpClient;
    private final HttpRequestParameters parameters;
    private final Gson                  gson;
    private final String                baseUrl;
    private final String                subsystemName;
    private final String                subsystemToken;
    ;

    public SSOHttpServiceApiClient(
            IHttpClient httpClient,
            HttpTimeouts httpTimeouts,
            String baseUrl,
            String subsystemName,
            String subsystemToken
    ) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.parameters = createRequestParameters(httpTimeouts);
        this.gson = new GsonBuilder().create();
        this.baseUrl = validateUrl(baseUrl);
        this.subsystemName = Objects.requireNonNull(subsystemName, "subsystemName must not be null");
        this.subsystemToken = Objects.requireNonNull(subsystemToken, "subsystemToken must not be null");
    }

    private HttpRequestParameters createRequestParameters(HttpTimeouts timeouts) {
        HttpTimeouts safeTimeouts = timeouts != null ? timeouts : new HttpTimeouts(30_000, 30_000);
        return HttpRequestParameters.builder()
                                    .timeouts(safeTimeouts)
                                    .build();
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    // Реализации методов интерфейса

    @Override
    public SSOUser authenticate(AuthenticateRequest request) throws SsoAuthException {
        return post("/authenticate", request, SSOUser.class);
    }

    @Override
    public boolean checkOtp(CheckOtpRequest request) throws SsoAuthException {
        return post("/checkOtp", request, BooleanResponse.class).isResult();
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) throws SsoAuthException {
        return post("/hasOtpMasterKey", request, BooleanResponse.class).isResult();
    }

    @Override
    public SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request) throws SsoAuthException {
        return post("/hasOtpMasterKey", request, SSOUser.class);
    }

    @Override
    public void sendSystemData(SendSystemDataRequest request) throws SsoSystemException {
        post("/sendSystemData", request, VoidResponse.class);
    }

    @Override
    public List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request) throws SsoDataException {
        return post(
                "/getUsersWithActions",
                request,
                new TypeToken<ListResponse<SSOUserWithActions>>() {
                }
        ).getData();
    }

    @Override
    public void updateUserOtpType(UpdateUserOtpTypeRequest request) throws SsoUserException {
        post("/updateUserOtpType", request, VoidResponse.class);
    }

    @Override
    public void registerUser(UserRegisterRequest request) throws UserExistsException, PolicyValidationException,
            BadPublicKeyException, MessageSendException {
        try {
            post("/registerUser", request, VoidResponse.class);
        } catch (SsoClientException e) {
            handleSpecificExceptions(e, request);
        }
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        try {
            post("/changeTempPassword", request, VoidResponse.class);
        } catch (SsoBadRequestException e) {
            throw new PolicyValidationException(e.getMessage());
        } catch (SsoClientException e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) throws SsoUserException {
        try {
            return post("/getUserDescription", request, UserDescription.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        }
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        try {
            StringResponse response = post("/resetGoogleAuthMasterKey", request, StringResponse.class);
            return response.getValue();
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (SsoServerException e) {
            throw new SsoDecryptException(e.getMessage());
        }
    }

    @Override
    public String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request) {
        try {
            StringResponse response = post("/getUrlToGoogleAuthQrCode", request, StringResponse.class);
            return response.getValue();
        } catch (SsoClientException e) {
            throw new RuntimeException("Failed to get QR code URL", e);
        }
    }

    @Override
    public void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request) throws SsoUserException {
        try {
            post("/updateUserIsOtpOptionalValue", request, VoidResponse.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        }
    }

    @Override
    public void updateUserDescription(UpdateUserDescriptionRequest request)
            throws UserNotFoundException, BadPublicKeyException {
        try {
            post("/updateUserDescription", request, VoidResponse.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (SsoBadRequestException e) {
            throw new BadPublicKeyException(e.getMessage());
        }
    }

    @Override
    public void resetPassword(PasswordResetRequest reset) throws UserNotFoundException, PolicyValidationException {
        try {
            post("/resetPassword", reset, VoidResponse.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (SsoBadRequestException e) {
            throw new PolicyValidationException(e.getMessage());
        }
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) throws SsoDataException {
        try {
            return post(
                    "/getUserStatuses",
                    request,
                    new TypeToken<ListResponse<UserStatus>>() {
                    }
            ).getData();
        } catch (SsoClientException e) {
            throw new SsoDataException("Failed to get user statuses", e);
        }
    }

    @Override
    public SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request) throws SsoAuthException {
        try {
            return post("/exchangeSubsystemToken", request, SSOUser.class);
        } catch (SsoUnauthorizedException e) {
            throw new SsoAuthException("Invalid subsystem token", e);
        }
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) throws SsoSystemException {
        try {
            post("/touchSessions", request, VoidResponse.class);
        } catch (SsoClientException e) {
            throw new SsoSystemException("Session touch failed", e);
        }
    }

    @Override
    public void completeUser(CompleteUserRequest request) throws SsoUserException {
        try {
            post("/completeUser", request, VoidResponse.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        }
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) throws SsoUserException {
        try {
            post("/changeUserRole", request, VoidResponse.class);
        } catch (SsoNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (SsoForbiddenException e) {
            throw new SsoUserException("Permission denied", e);
        }
    }

    private <T> T post(String endpoint, Object request, Class<T> responseClass) throws SsoClientException {
        return post(endpoint, request, TypeToken.get(responseClass));
    }

    private <T> T post(String endpoint, Object request, TypeToken<T> typeToken) throws SsoClientException {
        String url         = baseUrl + "/remoting/sso.service" + endpoint;
        String jsonRequest = gson.toJson(request);

        if (log.isDebugEnabled()) {
            log.debug("Sending request to {}: {}", url, jsonRequest);
        }

        HttpResponse response;
        try {
            response = httpClient.send(
                    buildRequest(url, jsonRequest),
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

    private HttpRequest buildRequest(String url, String jsonBody) {
        return HttpRequest.builder()
                          .url(url)
                          .method(HttpMethod.POST)
                          .headers(createHeaders())
                          .body(jsonBody.getBytes(StandardCharsets.UTF_8))
                          .build();
    }

    private HttpHeaders createHeaders() {
        return new HttpHeaders(List.of(
                new HttpHeader(HEADER_SUBSYSTEM_NAME, subsystemName),
                new HttpHeader(HEADER_SUBSYSTEM_TOKEN, subsystemToken),
                new HttpHeader(HEADER_HOST, subsystemToken),
                new HttpHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
        ));
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
        try {
            String json = new String(response.getBody(), StandardCharsets.UTF_8);
            return gson.fromJson(json, type);
        } catch (Exception e) {
            throw new SsoParseException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private void handleSpecificExceptions(SsoClientException e, Object request)
            throws UserExistsException, PolicyValidationException,
            BadPublicKeyException, MessageSendException {

        if (e instanceof SsoConflictException) {
            throw new UserExistsException(e.getMessage());
        }

        throw e;
    }

    // Вспомогательные классы для ответов

    @Getter
    private static class BooleanResponse {
        private boolean result;
    }

    @Getter
    private static class ListResponse<T> {
        private List<T> data;
    }

    private static class VoidResponse {
    }

    @Getter
    private static class StringResponse {
        private String value;
    }

}
