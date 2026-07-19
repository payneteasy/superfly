package com.payneteasy.superfly.api.client;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.payneteasy.http.client.api.HttpHeader;
import com.payneteasy.http.client.api.HttpHeaders;
import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.superfly.api.SSOEvent;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.UserRegisterRequest;
import com.payneteasy.superfly.api.UserStatus;
import com.payneteasy.superfly.api.exceptions.BadPublicKeyException;
import com.payneteasy.superfly.api.exceptions.MessageSendException;
import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.api.exceptions.SsoBadRequestException;
import com.payneteasy.superfly.api.exceptions.SsoClientException;
import com.payneteasy.superfly.api.exceptions.SsoConflictException;
import com.payneteasy.superfly.api.exceptions.SsoConnectionException;
import com.payneteasy.superfly.api.exceptions.SsoException;
import com.payneteasy.superfly.api.exceptions.SsoForbiddenException;
import com.payneteasy.superfly.api.exceptions.SsoNotFoundException;
import com.payneteasy.superfly.api.exceptions.SsoParseException;
import com.payneteasy.superfly.api.exceptions.SsoServerException;
import com.payneteasy.superfly.api.exceptions.SsoUnauthorizedException;
import com.payneteasy.superfly.api.exceptions.UserExistsException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.request.ChangeTempPasswordRequest;
import com.payneteasy.superfly.api.request.ChangeUserRoleRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.api.request.CompleteUserRequest;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import com.payneteasy.superfly.api.request.GetEventsRequest;
import com.payneteasy.superfly.api.request.GetGoogleAuthQrCodeRequest;
import com.payneteasy.superfly.api.request.GetUserDescriptionRequest;
import com.payneteasy.superfly.api.request.GetUserStatusesRequest;
import com.payneteasy.superfly.api.request.GetUsersWithActionsRequest;
import com.payneteasy.superfly.api.request.HasOtpMasterKeyRequest;
import com.payneteasy.superfly.api.request.PasswordResetRequest;
import com.payneteasy.superfly.api.request.PseudoAuthenticateRequest;
import com.payneteasy.superfly.api.request.ResetGoogleAuthMasterKeyRequest;
import com.payneteasy.superfly.api.request.SendSystemDataRequest;
import com.payneteasy.superfly.api.request.TouchSessionsRequest;
import com.payneteasy.superfly.api.request.UpdateUserDescriptionRequest;
import com.payneteasy.superfly.api.request.UpdateUserIsOtpOptionalValueRequest;
import com.payneteasy.superfly.api.request.UpdateUserOtpTypeRequest;
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

/**
 * HTTP-реализация {@link SSOService} над transport-абстракцией {@link IHttpClient}.
 *
 * <h2>Thread-safety</h2>
 * Класс immutable после конструирования — все поля {@code final}, конфигурация и заголовки
 * прекомпилированы один раз. Безопасен для использования из нескольких потоков, при условии
 * что нижележащий {@link IHttpClient} thread-safe.
 *
 * <h2>Lifecycle</h2>
 * Реализует {@link AutoCloseable}: {@link #close()} делегирует {@code close()} в нижележащий
 * transport, если тот тоже {@link AutoCloseable}. Используйте try-with-resources в local scope
 * или явно вызывайте {@code close()} при остановке приложения (через Spring {@code DisposableBean}
 * или DI-контейнер).
 *
 * <h2>Per-endpoint таймауты</h2>
 * Каждый вызов резолвит {@link HttpRequestParameters} через {@link SSOClientConfig#parametersFor(Endpoint)}.
 * Это позволяет назначать разные таймауты для критичного {@link Endpoint#AUTHENTICATE} (короткий)
 * и для {@link Endpoint#GET_EVENTS} (long-poll, длинный). См. Javadoc {@link SSOClientConfig}.
 *
 * <h2>Decode-path</h2>
 * Любой не-200 статус сначала проверяется на {@link ExceptionWrapper} в теле ответа — если сервер
 * прислал типизированное доменное исключение ({@link UserExistsException}, {@link PolicyValidationException}
 * и др.), оно прокидывается как есть. Если wrapper не распознан — fallback к
 * status-based исключению ({@link SsoBadRequestException}, {@link SsoUnauthorizedException}, …).
 */
@Slf4j
public final class SSOHttpServiceApiClient implements SSOService, AutoCloseable {

    private static final String HEADER_SUBSYSTEM_NAME  = "X-Subsystem-Name";
    private static final String HEADER_SUBSYSTEM_TOKEN = "X-Subsystem-Token";
    private static final String HEADER_CONTENT_TYPE    = "Content-Type";
    private static final String HEADER_ACCEPT          = "Accept";

    private final IHttpClient             httpClient;
    private final SSOClientConfig         config;
    private final ApiSerializationManager serializationManager;
    private final HttpHeaders             staticHeaders;

    /**
     * Primary конструктор с явной инъекцией зависимостей.
     *
     * @param httpClient           transport-адаптер (например {@code HttpClientImpl} или Apache HC5).
     * @param config               конфигурация SSO-клиента (URL, subsystem, per-endpoint params).
     * @param serializationManager сериализатор тела запросов/ответов.
     */
    public SSOHttpServiceApiClient(
            IHttpClient httpClient,
            SSOClientConfig config,
            ApiSerializationManager serializationManager
    ) {
        this.httpClient           = Objects.requireNonNull(httpClient,           "httpClient must not be null");
        this.config               = Objects.requireNonNull(config,               "config must not be null");
        this.serializationManager = Objects.requireNonNull(serializationManager, "serializationManager must not be null");
        this.staticHeaders        = buildStaticHeaders(config);
        log.debug("SSOHttpServiceApiClient initialized: baseUrl={} subsystem={} transport={}",
                config.getBaseUrl(), config.getSubsystemName(), httpClient.getClass().getSimpleName());
    }

    // ====================================================================
    // SSOService interface methods — все делегируют в invoke(Endpoint, ...)
    // ====================================================================

    @Override
    public SSOUser authenticate(AuthenticateRequest request) {
        return invoke(Endpoint.AUTHENTICATE, request, SSOUser.class);
    }

    @Override
    public boolean checkOtp(CheckOtpRequest request) throws SsoDecryptException {
        return invoke(Endpoint.CHECK_OTP, request, Boolean.class);
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) {
        return invoke(Endpoint.HAS_OTP_MASTER_KEY, request, Boolean.class);
    }

    @Override
    public SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request) {
        return invoke(Endpoint.PSEUDO_AUTHENTICATE, request, SSOUser.class);
    }

    @Override
    public void sendSystemData(SendSystemDataRequest request) {
        invoke(Endpoint.SEND_SYSTEM_DATA, request, Void.class);
    }

    @Override
    public List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request) {
        return invoke(Endpoint.GET_USERS_WITH_ACTIONS, request, new TypeToken<>() {});
    }

    @Override
    public void updateUserOtpType(UpdateUserOtpTypeRequest request) {
        invoke(Endpoint.UPDATE_USER_OTP_TYPE, request, Void.class);
    }

    @Override
    public void registerUser(UserRegisterRequest request)
            throws UserExistsException, PolicyValidationException, BadPublicKeyException, MessageSendException {
        invoke(Endpoint.REGISTER_USER, request, Void.class);
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        invoke(Endpoint.CHANGE_TEMP_PASSWORD, request, Void.class);
    }

    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) {
        return invoke(Endpoint.GET_USER_DESCRIPTION, request, UserDescription.class);
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        return invoke(Endpoint.RESET_GOOGLE_AUTH_MASTER_KEY, request, String.class);
    }

    @Override
    public String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request) {
        return invoke(Endpoint.GET_URL_TO_GOOGLE_AUTH_QR_CODE, request, String.class);
    }

    @Override
    public void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request) {
        invoke(Endpoint.UPDATE_USER_IS_OTP_OPTIONAL_VALUE, request, Void.class);
    }

    @Override
    public void updateUserDescription(UpdateUserDescriptionRequest request)
            throws UserNotFoundException, BadPublicKeyException {
        invoke(Endpoint.UPDATE_USER_DESCRIPTION, request, Void.class);
    }

    @Override
    public void resetPassword(PasswordResetRequest reset)
            throws UserNotFoundException, PolicyValidationException {
        invoke(Endpoint.RESET_PASSWORD, reset, Void.class);
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) {
        return invoke(Endpoint.GET_USER_STATUSES, request, new TypeToken<>() {});
    }

    @Override
    public SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request) {
        return invoke(Endpoint.EXCHANGE_SUBSYSTEM_TOKEN, request, SSOUser.class);
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) {
        invoke(Endpoint.TOUCH_SESSIONS, request, Void.class);
    }

    @Override
    public void completeUser(CompleteUserRequest request) {
        invoke(Endpoint.COMPLETE_USER, request, Void.class);
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) {
        invoke(Endpoint.CHANGE_USER_ROLE, request, Void.class);
    }

    @Override
    public List<SSOEvent> getEvents(GetEventsRequest request) {
        return invoke(Endpoint.GET_EVENTS, request, new TypeToken<>() {});
    }

    // ====================================================================
    // Core: invoke → send → decode
    // ====================================================================

    private <T> T invoke(Endpoint endpoint, Object body, Class<T> responseClass) {
        return invoke(endpoint, body, TypeToken.get(responseClass));
    }

    private <T> T invoke(Endpoint endpoint, Object body, TypeToken<T> typeToken) {
        HttpRequestParameters parameters = config.parametersFor(endpoint);
        HttpResponse          response   = send(endpoint, body, parameters);
        return decode(endpoint, response, typeToken);
    }

    private HttpResponse send(Endpoint endpoint, Object body, HttpRequestParameters parameters) {
        String url         = config.urlFor(endpoint);
        String requestBody = serializationManager.serialize(body);

        log.debug("invoke endpoint={} url={} body.length={}", endpoint, url, requestBody.length());

        HttpRequest request = HttpRequest.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(staticHeaders)
                .body(requestBody.getBytes(StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse response = httpClient.send(request, parameters);
            log.debug("Response endpoint={} status={} body.length={}",
                    endpoint, response.getStatusCode(),
                    response.getBody() == null ? 0 : response.getBody().length);
            return response;
        } catch (HttpConnectException | HttpWriteException | HttpReadException e) {
            log.debug("Connection error endpoint={}: {}", endpoint, e.getMessage());
            throw new SsoConnectionException("Connection error to " + endpoint + ": " + e.getMessage(), e);
        }
    }

    /**
     * Унифицированный decode-path:
     * <ol>
     *   <li>{@code status == 200} → десериализация тела как {@code T}.</li>
     *   <li>Любой другой статус (включая 4xx/5xx и 2xx-non-200) → попытка распарсить тело как
     *       {@link ExceptionWrapper}; если успешно — пробросить типизированное серверное исключение
     *       через {@link ExceptionSerializationHelper#createException}.</li>
     *   <li>Иначе fallback к status-based исключению ({@link SsoBadRequestException},
     *       {@link SsoUnauthorizedException}, …, {@link SsoServerException}, generic {@link SsoClientException}).</li>
     * </ol>
     *
     * <p><b>Bug fix:</b> Предыдущая реализация бросала generic status-based исключение до того,
     * как могла попытаться распарсить {@link ExceptionWrapper} для 4xx/5xx — серверные доменные
     * исключения ({@link UserExistsException}, {@link PolicyValidationException}) терялись и
     * заменялись на generic {@link SsoBadRequestException}. Теперь wrapper парсится первым.
     */
    private <T> T decode(Endpoint endpoint, HttpResponse response, TypeToken<T> typeToken) {
        int    status      = response.getStatusCode();
        String body        = response.getBody() == null ? "" : new String(response.getBody(), StandardCharsets.UTF_8);
        String contentType = resolveContentType(response);

        if (status == 200) {
            log.debug("decode endpoint={} status=200 → success path", endpoint);
            return deserialize(body, typeToken.getType(), contentType);
        }

        ExceptionWrapper wrapper = tryParseExceptionWrapper(body, contentType);
        if (wrapper != null && wrapper.getExceptionClass() != null) {
            log.debug("decode endpoint={} status={} → ExceptionWrapper class={} message={}",
                    endpoint, status, wrapper.getExceptionClass(), wrapper.getMessage());
            Throwable recreated = ExceptionSerializationHelper.createException(wrapper);
            if (recreated instanceof RuntimeException re) {
                throw re;
            }
            throw new SsoParseException("Server exception is not a RuntimeException: " + recreated.getMessage(), recreated);
        }

        log.debug("decode endpoint={} status={} → no wrapper, fallback to status-based exception", endpoint, status);
        throw statusToException(endpoint, status, body);
    }

    @Nullable
    private ExceptionWrapper tryParseExceptionWrapper(String body, String contentType) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            Object parsed = serializationManager.deserialize(body, ExceptionWrapper.class, contentType);
            return parsed instanceof ExceptionWrapper ew ? ew : null;
        } catch (JsonSyntaxException e) {
            log.debug("Body is not valid ExceptionWrapper JSON: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.debug("Failed to parse body as ExceptionWrapper: {}", e.getMessage());
            return null;
        }
    }

    private <T> T deserialize(String body, Type type, String contentType) {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) serializationManager.deserialize(body, type, contentType);
            return result;
        } catch (SsoException e) {
            throw e;
        } catch (Exception e) {
            throw new SsoParseException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private static RuntimeException statusToException(Endpoint endpoint, int status, String body) {
        String message = String.format("HTTP error %d from %s: %s", status, endpoint, body);
        return switch (status) {
            case 400 -> new SsoBadRequestException(message);
            case 401 -> new SsoUnauthorizedException(message);
            case 403 -> new SsoForbiddenException(message);
            case 404 -> new SsoNotFoundException(message);
            case 409 -> new SsoConflictException(message);
            default  -> status >= 500 ? new SsoServerException(message) : new SsoClientException(status, message);
        };
    }

    private String resolveContentType(HttpResponse response) {
        return response.getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase(HEADER_CONTENT_TYPE))
                .findFirst()
                .map(HttpHeader::getValue)
                .orElseGet(serializationManager::getDefaultContentType);
    }

    private static HttpHeaders buildStaticHeaders(SSOClientConfig config) {
        List<HttpHeader> headers = new ArrayList<>(4);
        headers.add(new HttpHeader(HEADER_SUBSYSTEM_NAME, config.getSubsystemName()));
        headers.add(new HttpHeader(HEADER_CONTENT_TYPE,   CONTENT_TYPE_JSON));
        headers.add(new HttpHeader(HEADER_ACCEPT,         CONTENT_TYPE_JSON));
        if (config.getSubsystemToken() != null) {
            headers.add(new HttpHeader(HEADER_SUBSYSTEM_TOKEN, config.getSubsystemToken()));
        }
        return new HttpHeaders(headers);
    }

    // ====================================================================
    // AutoCloseable
    // ====================================================================

    /**
     * Закрывает нижележащий transport, если он реализует {@link AutoCloseable}.
     * Без побочных эффектов, если transport не {@link AutoCloseable}.
     */
    @Override
    public void close() throws Exception {
        log.debug("Closing SSO client, transport={}", httpClient.getClass().getSimpleName());
        if (httpClient instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }
}
