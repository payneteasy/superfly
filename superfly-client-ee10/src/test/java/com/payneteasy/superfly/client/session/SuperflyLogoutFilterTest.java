package com.payneteasy.superfly.client.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SuperflyLogoutFilterTest {

    private static final String TOKEN = "dummy-subsystem-token";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private LogoutService logoutService;
    private SuperflyLogoutFilter filter;

    @Before
    public void setUp() {
        request = EasyMock.createMock(HttpServletRequest.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        chain = EasyMock.createMock(FilterChain.class);
        logoutService = EasyMock.createMock(LogoutService.class);
        filter = new SuperflyLogoutFilter(logoutService);
        filter.setNotificationSecret(TOKEN);
    }

    @Test
    public void doFilter_WhenNotPostRequest_ShouldContinueChain() throws IOException, ServletException {
        // Configure expected behavior
        EasyMock.expect(request.getMethod()).andReturn("GET");
        chain.doFilter(request, response);

        // Enable all mocks in replay mode
        EasyMock.replay(request, response, chain, logoutService);

        // Execute the tested method
        filter.doFilter(request, response, chain);

        // Verify that all expected calls were made
        EasyMock.verify(request, response, chain, logoutService);
    }

    @Test
    public void doFilter_WhenPostButNoLogoutParam_ShouldContinueChain() throws IOException, ServletException {
        // Configure expected behavior
        EasyMock.expect(request.getMethod()).andReturn("POST");
        EasyMock.expect(request.getParameter(LogoutService.LOGOUT_SESSION_IDS_PARAM)).andReturn(null);
        chain.doFilter(request, response);

        // Включаем все моки в режим воспроизведения
        EasyMock.replay(request, response, chain, logoutService);

        // Выполняем тестируемый метод
        filter.doFilter(request, response, chain);

        // Проверяем, что все ожидаемые вызовы выполнены
        EasyMock.verify(request, response, chain, logoutService);
    }

    @Test
    public void doFilter_WhenLogoutSuccessful_ShouldNotContinueChain() throws IOException, ServletException {
        // Configure expected behavior
        EasyMock.expect(request.getMethod()).andReturn("POST");
        EasyMock.expect(request.getParameter(LogoutService.LOGOUT_SESSION_IDS_PARAM)).andReturn("session1,session2");
        expectSignedNotification("session1,session2");
        EasyMock.expect(logoutService.handleLogout("session1,session2")).andReturn(true);

        // Включаем все моки в режим воспроизведения
        EasyMock.replay(request, response, chain, logoutService);

        // Выполняем тестируемый метод
        filter.doFilter(request, response, chain);

        // Проверяем, что все ожидаемые вызовы выполнены
        EasyMock.verify(request, response, chain, logoutService);
    }

    @Test
    public void doFilter_WhenLogoutNotSuccessful_ShouldContinueChain() throws IOException, ServletException {
        // Configure expected behavior
        EasyMock.expect(request.getMethod()).andReturn("POST");
        EasyMock.expect(request.getParameter(LogoutService.LOGOUT_SESSION_IDS_PARAM)).andReturn("session1,session2");
        expectSignedNotification("session1,session2");
        EasyMock.expect(logoutService.handleLogout("session1,session2")).andReturn(false);
        chain.doFilter(request, response);

        // Включаем все моки в режим воспроизведения
        EasyMock.replay(request, response, chain, logoutService);

        // Выполняем тестируемый метод
        filter.doFilter(request, response, chain);

        // Проверяем, что все ожидаемые вызовы выполнены
        EasyMock.verify(request, response, chain, logoutService);
    }

    private void expectSignedNotification(String logoutSessionIds) {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put(LogoutService.LOGOUT_SESSION_IDS_PARAM, new String[]{logoutSessionIds});
        params.put(NotificationSignatures.TIMESTAMP_PARAMETER, new String[]{String.valueOf(System.currentTimeMillis())});
        params.put(NotificationSignatures.SIGNATURE_PARAMETER,
                new String[]{NotificationSignatures.sign(SubsystemTokenHashes.hash(TOKEN), params)});
        EasyMock.expect(request.getParameterMap()).andReturn(params);
        EasyMock.expect(request.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
    }
}
