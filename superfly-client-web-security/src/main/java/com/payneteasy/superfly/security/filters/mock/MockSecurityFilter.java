package com.payneteasy.superfly.security.filters.mock;

import com.payneteasy.superfly.security.spring.internal.SecurityContext;
import com.payneteasy.superfly.security.filters.ExcludedPaths;
import com.payneteasy.superfly.security.filters.internal.SecurityFilterFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static com.payneteasy.superfly.security.spring.internal.SecurityContextStore.setToSession;

public class MockSecurityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(MockSecurityFilter.class);

    private final ExcludedPaths paths;
    private final MockUserDatabase userDatabase;

    public MockSecurityFilter(ExcludedPaths aPaths, MockUserDatabase aUserDatabase) {
        paths = aPaths;
        userDatabase = aUserDatabase;
    }

    @Override
    public void init(FilterConfig aConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain) throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest) aRequest;
        HttpServletResponse response = (HttpServletResponse) aResponse;

        SecurityFilterFlow flow = new SecurityFilterFlow(request, response);
        String url = flow.getPath();

        if(flow.processLogoutUrl()) {
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

        if(isSubmittingUsernameAndPasswordUrl(url)) {
            SecurityContext context = checkUsernameAndPassword(request);
            if(context == null) {
                showLoginPage(response, "Bad username or password");
                return;
            }
            setToSession(context, request);
            redirectToStartPage(request.getContextPath(), response);
            return;
        }

        showLoginPage(response);
    }

    private void showLoginPage(HttpServletResponse aResponse) throws IOException {
        showLoginPage(aResponse, null);
    }
    private void showLoginPage(HttpServletResponse aResponse, String aErrorMessage) throws IOException {
        PrintWriter out = aResponse.getWriter();
        if(aErrorMessage != null) {
            out.write("<div class='error'>");
            out.write(aErrorMessage);
            out.write("</>");
        }


        String text = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\" />" +
                "  <link rel='stylesheet' href='css/login.css' />\n"
                +"<div class='superfly-login-page'>\n" +
                "  <div class='superfly-subsystem-info'>\n" +
                "    Logging in to <span class='superfly-subsystem-title'>Business Lounge UI</span>\n" +
                "  </div>\n" +
                "</head>\n" +
                "  <h2>Authentication</h2>\n" +
                "  <form action='/j_superfly_password_security_check' method='POST'>\n" +
                "    <div class='superfly-form-row'>\n" +
                "      <label class='superfly-form-label' for='j_username'>Username</label>\n" +
                "      <input class='superfly-form-input-text' id='j_username' value='' name='j_username' maxlength='32'>\n" +
                "    </div>\n" +
                "    <div class='superfly-form-row'>\n" +
                "      <label class='superfly-form-label' for='j_password'>Password</label>\n" +
                "      <input class='superfly-form-input-text' id='j_password' type='password' value='' name='j_password' autocomplete='off' maxlength='32'>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class='superfly-button-row'>\n" +
                "      <input class='superfly-button' type='submit' value='Login'>\n" +
                "    </div>\n" +
                "  </form>\n" +
                "</div>\n";
        out.write(text);
    }

    private void redirectToStartPage(String aStartUrl, HttpServletResponse aResponse) throws IOException {
        aResponse.sendRedirect(aStartUrl);
    }

    private SecurityContext checkUsernameAndPassword(HttpServletRequest aRequest) {
        String username = aRequest.getParameter("j_username");
        String password = aRequest.getParameter("j_password");
        Set<String> actions = userDatabase.checkUsernameAndPassword(username, password);
        return actions!= null ? new SecurityContext(username, actions) : null; 
    }

    private boolean isSubmittingUsernameAndPasswordUrl(String aUrl) {
        return aUrl.startsWith("/j_superfly_password_security_check");
    }

    @Override
    public void destroy() {
    }
}
