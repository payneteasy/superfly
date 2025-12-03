package com.payneteasy.superfly.web.mvc;

import com.payneteasy.superfly.service.RemoteAuthService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.web.mvc.model.CheckOtpRequest;
import com.payneteasy.superfly.web.mvc.model.CheckOtpResponse;
import com.payneteasy.superfly.web.mvc.model.CheckPasswordRequest;
import com.payneteasy.superfly.web.mvc.model.CheckPasswordResponse;
import com.payneteasy.superfly.web.mvc.model.ErrorResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RemoteAuthCheckControllerTest {

    private RemoteAuthCheckController controller;
    private RemoteAuthService         remoteAuthService;
    private HttpServletRequest        request;

    @Before
    public void setUp() {
        remoteAuthService = createMock(RemoteAuthService.class);
        request = createMock(HttpServletRequest.class);
        // Внедрение зависимостей через конструктор, что позволяет отказаться от @Autowired на сеттерах
        // и делает тестирование проще (не нужен Spring Context в юнит-тесте)
        controller = new RemoteAuthCheckController(remoteAuthService);
    }

    @Test
    public void testCheckPasswordSuccess() throws Exception {
        // 1. Подготовка данных (Fixtures)
        // Используем константы и переменные для лучшей читаемости и повторного использования
        String subsystemName     = "test-subsystem";
        String username          = "test-user";
        String bearerToken       = "test-token";
        String passwordEncrypted = "encrypted-password";
        String ipAddress         = "127.0.0.1";
        String userAgent         = "TestAgent";
        String sessionToken      = "session-123";

        // Создаем объект запроса через сеттеры (или можно было бы через конструктор/builder если бы они были)
        CheckPasswordRequest requestBody = new CheckPasswordRequest();
        requestBody.setUsername(username);
        requestBody.setPasswordEncrypted(passwordEncrypted);

        // 2. Программирование моков (Record phase)
        // Ожидаем вызовы к request (получение IP и User-Agent)
        expect(request.getRemoteAddr()).andReturn(ipAddress);
        expect(request.getHeader("User-Agent")).andReturn(userAgent);

        // Ожидаем вызов бизнес-логики сервиса
        expect(remoteAuthService.checkPassword(subsystemName, username, passwordEncrypted, bearerToken, ipAddress, userAgent))
                .andReturn(new RemoteAuthService.RemoteAuthSession(sessionToken, false));

        // 3. Перевод моков в режим воспроизведения (Replay phase)
        replay(remoteAuthService, request);

        // 4. Выполнение тестируемого кода (Exercise)
        CheckPasswordResponse response = controller.checkPassword(subsystemName, username, requestBody, "Bearer " + bearerToken, request);

        // 5. Проверка вызовов (Verify phase)
        verify(remoteAuthService, request);

        // 6. Проверка результата (Assertions)
        assertEquals("Username should match", username, response.getUsername());
        assertEquals("Session token should match", sessionToken, response.getSessionToken());
        assertFalse("Otp required should be false", response.isOtpRequired());
    }

    @Test
    public void testCheckOtpSuccess() throws Exception {
        String subsystemName = "test-subsystem";
        String username      = "test-user";
        String bearerToken   = "test-token";
        String otpEncrypted  = "encrypted-otp";
        String sessionToken  = "session-123";
        String authResult    = "SUCCESS";

        CheckOtpRequest requestBody = new CheckOtpRequest();
        requestBody.setUsername(username);
        requestBody.setOtpEncrypted(otpEncrypted);
        requestBody.setSessionToken(sessionToken);

        expect(remoteAuthService.checkOtp(subsystemName, username, otpEncrypted, sessionToken, bearerToken))
                .andReturn(authResult);

        replay(remoteAuthService, request);

        CheckOtpResponse response = controller.checkOtp(subsystemName, username, requestBody, "Bearer " + bearerToken);

        verify(remoteAuthService, request);

        assertEquals(username, response.getUsername());
        assertEquals(sessionToken, response.getSessionToken());
        assertEquals(authResult, response.getResult());
    }

    @Test(expected = RemoteAuthCheckController.UnauthorizedException.class)
    public void testCheckPasswordMissingToken() throws RemoteAuthException {
        String subsystemName = "test-subsystem";
        String username      = "test-user";
        CheckPasswordRequest requestBody = new CheckPasswordRequest();

        replay(remoteAuthService, request);
        controller.checkPassword(subsystemName, username, requestBody, null, request);
    }

    @Test(expected = RemoteAuthCheckController.BadRequestException.class)
    public void testCheckPasswordMissingFields() throws RemoteAuthException {
        String subsystemName = "test-subsystem";
        String username      = "test-user";
        CheckPasswordRequest requestBody = new CheckPasswordRequest();
        requestBody.setUsername(username);
        // Missing password

        replay(remoteAuthService, request);
        controller.checkPassword(subsystemName, username, requestBody, "Bearer token", request);
    }

    @Test(expected = RemoteAuthException.class)
    public void testCheckPasswordServiceException() throws Exception {
        String subsystemName     = "test-subsystem";
        String username          = "test-user";
        String bearerToken       = "test-token";
        String passwordEncrypted = "encrypted-password";
        String ipAddress         = "127.0.0.1";
        String userAgent         = "TestAgent";

        CheckPasswordRequest requestBody = new CheckPasswordRequest();
        requestBody.setUsername(username);
        requestBody.setPasswordEncrypted(passwordEncrypted);

        expect(request.getRemoteAddr()).andReturn(ipAddress);
        expect(request.getHeader("User-Agent")).andReturn(userAgent);
        expect(remoteAuthService.checkPassword(subsystemName, username, passwordEncrypted, bearerToken, ipAddress, userAgent))
                .andThrow(new RemoteAuthException("Some error", "INTERNAL_ERROR"));

        replay(remoteAuthService, request);

        controller.checkPassword(subsystemName, username, requestBody, "Bearer " + bearerToken, request);
    }

    @Test
    public void testHandleRemoteAuthException() {
        RemoteAuthException ex = new RemoteAuthException("Error message", "INTERNAL_ERROR");
        ResponseEntity<ErrorResponse> response = controller.handleRemoteAuthException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getType());
        assertEquals("Error message", response.getBody().getTitle());
    }

    @Test
    public void testHandleRemoteAuthExceptionBadRequest() {
        RemoteAuthException ex = new RemoteAuthException("Bad credentials", "BAD_CREDENTIALS");
        ResponseEntity<ErrorResponse> response = controller.handleRemoteAuthException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_CREDENTIALS", response.getBody().getType());
    }

    @Test
    public void testHandleUnauthorized() {
        RemoteAuthCheckController.UnauthorizedException ex = new RemoteAuthCheckController.UnauthorizedException("Unauthorized access");
        ResponseEntity<ErrorResponse> response = controller.handleUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getType());
        assertEquals("Unauthorized access", response.getBody().getTitle());
    }

    @Test
    public void testHandleBadRequest() {
        RemoteAuthCheckController.BadRequestException ex = new RemoteAuthCheckController.BadRequestException("Bad request data");
        ResponseEntity<ErrorResponse> response = controller.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getType());
        assertEquals("Bad request data", response.getBody().getTitle());
    }

    @Test
    public void testHandleJsonError() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Json error");
        ResponseEntity<ErrorResponse> response = controller.handleJsonError(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getType());
        assertEquals("Invalid JSON", response.getBody().getTitle());
    }
}
