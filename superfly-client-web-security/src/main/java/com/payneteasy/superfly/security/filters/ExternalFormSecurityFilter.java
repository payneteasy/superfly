package com.payneteasy.superfly.security.filters;

import com.caucho.hessian.client.HessianProxyFactory;
import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.filters.internal.SecurityFilterFlow;
import com.payneteasy.superfly.security.spring.SecuredBeanPostProcessor;
import com.payneteasy.superfly.security.spring.internal.SecurityContext;
import com.payneteasy.superfly.security.spring.internal.SecurityContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ExternalFormSecurityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalFormSecurityFilter.class);

    private final ExcludedPaths paths;
    private final String loginFormUrl;
    private final String logoutUrl;
    private final SSOService    ssoService;
    private final String packageName;
    private final String systemName;

    public ExternalFormSecurityFilter(ExcludedPaths aPaths
            , String aSystemName
            , String aAccessToken
            , String aSsoWebBaseUrl
            , String aPackageName
            , String aSsoServiceBaseUrl
    ) {
        paths = aPaths;
        loginFormUrl = aSsoWebBaseUrl + "/sso/login?subsystemIdentifier=" + aSystemName + "&targetUrl=";
        logoutUrl    = aSsoWebBaseUrl + "/sso/logout?subsystemIdentifier=" + aSystemName + "&targetUrl=";
        packageName = aPackageName;
        systemName = aSystemName;

        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser(aSystemName);
        factory.setPassword(aAccessToken);
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(30_000);
        String serviceUrl = aSsoServiceBaseUrl + "/remoting/basic.hessian.service";
        try {
            ssoService = (SSOService) factory.create(SSOService.class, serviceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not parse url: " + serviceUrl, e);
        }

    }

    @Override
    public void init(FilterConfig aFilter) throws ServletException {
        try {
            ActionDescription[] actions = getActionDescriptions();
            LOG.info("Sending {} actions to sso ...", actions.length);
            ssoService.sendSystemData(systemName, actions);
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
                showBadTokenPage(response, e.getMessage());
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
        LOG.info("Checking token {}", subsystemToken);
        SSOUser ssoUser = ssoService.exchangeSubsystemToken(subsystemToken);
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
    }
}
