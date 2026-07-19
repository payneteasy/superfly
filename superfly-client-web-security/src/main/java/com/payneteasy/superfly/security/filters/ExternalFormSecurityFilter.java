package com.payneteasy.superfly.security.filters;

import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.impl.HttpClientImpl;
import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import com.payneteasy.superfly.api.request.SendSystemDataRequest;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.client.SSOClientConfig;
import com.payneteasy.superfly.api.client.SSOHttpServiceApiClient;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.security.filters.internal.SecurityFilterFlow;
import com.payneteasy.superfly.security.spring.SecuredBeanPostProcessor;
import com.payneteasy.superfly.security.spring.internal.SecurityContext;
import com.payneteasy.superfly.security.spring.internal.SecurityContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ExternalFormSecurityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalFormSecurityFilter.class);

    private static final int DEFAULT_TIMEOUT_MS = 30_000;

    /** Path-сегмент JSON remote-api на стороне SSO-сервера (RemoteApiController @RequestMapping). */
    private static final String REMOTE_API_PATH = "/remoting/sso.service";

    private final ExcludedPaths paths;
    private final String loginFormUrl;
    private final String logoutUrl;
    private final SSOService    ssoService;
    private final String packageName;
    private final String systemName;
    /** true, если транспортный JSON-клиент создан этим фильтром и должен закрываться в {@link #destroy()}. */
    private final boolean ownsSsoService;

    /**
     * DI-конструктор: {@link SSOService} инжектится снаружи (тесты / Spring-конфигурация).
     * Жизненным циклом клиента управляет вызывающая сторона — {@link #destroy()} его не закрывает.
     */
    public ExternalFormSecurityFilter(ExcludedPaths aPaths
            , String aSystemName
            , String aSsoWebBaseUrl
            , String aPackageName
            , SSOService aSsoService
    ) {
        this(aPaths, aSystemName, aSsoWebBaseUrl, aPackageName, aSsoService, false);
    }

    /**
     * URL-конструктор (обратная совместимость для интеграторов): собирает JSON-клиент
     * {@link SSOHttpServiceApiClient} поверх {@link HttpClientImpl}, заменяя прежний Hessian-транспорт.
     *
     * @param aSsoServiceBaseUrl базовый URL SSO-сервера (HTTPS; добавляется {@value #REMOTE_API_PATH}).
     *                           Допускается http только при {@code -Dsuperfly.client.allowInsecureScheme=true}.
     */
    public ExternalFormSecurityFilter(ExcludedPaths aPaths
            , String aSystemName
            , String aAccessToken
            , String aSsoWebBaseUrl
            , String aPackageName
            , String aSsoServiceBaseUrl
    ) {
        this(aPaths, aSystemName, aSsoWebBaseUrl, aPackageName,
                buildJsonClient(aSystemName, aAccessToken, aSsoServiceBaseUrl), true);
    }

    private ExternalFormSecurityFilter(ExcludedPaths aPaths
            , String aSystemName
            , String aSsoWebBaseUrl
            , String aPackageName
            , SSOService aSsoService
            , boolean aOwnsSsoService
    ) {
        paths = aPaths;
        loginFormUrl = aSsoWebBaseUrl + "/sso/login?subsystemIdentifier=" + aSystemName + "&targetUrl=";
        logoutUrl    = aSsoWebBaseUrl + "/sso/logout?subsystemIdentifier=" + aSystemName + "&targetUrl=";
        packageName = aPackageName;
        systemName = aSystemName;
        ssoService = Objects.requireNonNull(aSsoService, "ssoService must not be null");
        ownsSsoService = aOwnsSsoService;
        LOG.debug("ExternalFormSecurityFilter initialized: system={} ownsClient={}", aSystemName, aOwnsSsoService);
    }

    private static SSOService buildJsonClient(String aSystemName, String aAccessToken, String aSsoServiceBaseUrl) {
        String baseUrl = aSsoServiceBaseUrl + REMOTE_API_PATH;
        LOG.info("Building JSON SSO client: baseUrl={} subsystem={}", baseUrl, aSystemName);
        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl(baseUrl)
                .subsystemName(aSystemName)
                .subsystemToken(aAccessToken)
                .defaultParameters(HttpRequestParameters.builder()
                        .timeouts(new HttpTimeouts(DEFAULT_TIMEOUT_MS, DEFAULT_TIMEOUT_MS))
                        .build())
                .build();
        return new SSOHttpServiceApiClient(new HttpClientImpl(), config, new ApiSerializationManager());
    }

    @Override
    public void init(FilterConfig aFilter) throws ServletException {
        try {
            ActionDescription[] actions = getActionDescriptions();
            LOG.info("Sending {} actions to sso ...", actions.length);
            ssoService.sendSystemData(new SendSystemDataRequest(systemName, List.of(actions)));
        } catch (Exception e) {
            LOG.error("Unable to send action to sso service", e);
            throw new ServletException("Unable to send action to sso service", e);
        }
    }

    private ActionDescription[] getActionDescriptions() {
        String[] actions = SecuredBeanPostProcessor.getCollectedActions();
        ActionDescription[] descriptions = new ActionDescription[actions.length];
        for(int i=0; i<actions.length; i++) {
            descriptions[i] = new ActionDescription(actions[i], null);
        }
        return descriptions;
    }

    @Override
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain) throws IOException, ServletException {

        HttpServletRequest request   = (HttpServletRequest) aRequest;
        HttpServletResponse response = (HttpServletResponse) aResponse;

        SecurityFilterFlow flow = new SecurityFilterFlow(request, response);

        if(processLogoutUrl(flow.getPath(), request, response)) {
            LOG.debug("Logout url");
            return;
        }

        if(flow.processWithSecurityContext(aChain)) {
            LOG.debug("Process with security context");
            return;
        }

        if(flow.processExcluded(paths, aChain)) {
            LOG.debug("Process excluded urls");
            return;
        }

        if(flow.getPath().equals("/check-token") || flow.getPath().equals("/j_superfly_sso_security_check")) {
            try {
                validateExternalToken(request);
                response.sendRedirect(request.getParameter("targetUrl"));
            } catch (Exception e) {
                LOG.error("Could not validate token", e);
                showBadTokenPage(response, "Token validation failed");
            }
            return;
        }

        redirectToLoginPage(request.getRequestURI(), response);
    }

    public boolean processLogoutUrl(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(path.startsWith("/j_spring_security_logout")) {
            SecurityContextStore.clearFromSession(request);
            response.sendRedirect(logoutUrl + URLEncoder.encode(request.getContextPath(), StandardCharsets.UTF_8));
            return true;
        }
        return false;
    }

    private void showBadTokenPage(HttpServletResponse aResponse, String aMessage) throws IOException {
        aResponse.getWriter().println(aMessage);
    }

    private void validateExternalToken(HttpServletRequest aRequest) {
        String subsystemToken = aRequest.getParameter("subsystemToken");
        if(subsystemToken == null) {
            throw new IllegalStateException("No 'subsystemToken' in paraters");
        }
        LOG.debug("Checking subsystem token");
        SSOUser ssoUser = ssoService.exchangeSubsystemToken(new ExchangeSubsystemTokenRequest(subsystemToken));
        if(ssoUser == null) {
            throw new IllegalStateException("Token is not valid");
        }

        SecurityContext context = createContextFromUser(ssoUser);
        LOG.info("Got security context: {}", context);
        SecurityContextStore.setToSession(context, aRequest);
    }

    private SecurityContext createContextFromUser(SSOUser aUser) {
        SSOAction[] ssoActions = aUser.getActionsMap().values().iterator().next();
        Set<String> actions = new HashSet<>();
        for (SSOAction ssoAction : ssoActions) {
            actions.add(ssoAction.getName().toUpperCase());
        }
        return new SecurityContext(aUser.getName(), actions);
    }

    private void redirectToLoginPage(String aUrl, HttpServletResponse aResponse) throws IOException {
        String formUrl = loginFormUrl + URLEncoder.encode(aUrl, StandardCharsets.UTF_8);
        LOG.debug("Sending redirect to external form to {}", formUrl);
        aResponse.sendRedirect(formUrl);
    }

    @Override
    public void destroy() {
        if (ownsSsoService && ssoService instanceof AutoCloseable closeable) {
            try {
                LOG.debug("Closing owned JSON SSO client");
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Error while closing SSO client", e);
            }
        }
    }
}
