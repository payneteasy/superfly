package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.exception.AuthenticationCarrier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * {@link AuthenticationEntryPoint} which is intended to be used for multi-step
 * authentication process. It redirects to URLs determined by the current
 * {@link Authentication} class.
 *
 * @author Roman Puchkovskiy
 */
public class MultiStepLoginUrlAuthenticationEntryPoint extends
        LoginUrlAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private Map<Class<? extends Authentication>, String> stepInsufficientAuthenticationMapping = Collections.emptyMap();

    public MultiStepLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    public void setInsufficientAuthenticationMapping(
            Map<Class<? extends Authentication>,
            String> mapping) {
        this.stepInsufficientAuthenticationMapping = mapping;
    }

    @Override
    protected String determineUrlToUseForThisRequest(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
        String url = null;
        Authentication auth = null;
        if (exception instanceof AuthenticationCarrier) {
            auth = ((AuthenticationCarrier) exception).getAuthentication();
        }
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }
        if (auth != null) {
            Authentication authToChooseUrl;
            if (auth instanceof CompoundAuthentication) {
                CompoundAuthentication compound = (CompoundAuthentication) auth;
                authToChooseUrl = compound.getLatestReadyAuthentication();
            } else {
                authToChooseUrl = auth;
            }
            url = stepInsufficientAuthenticationMapping.get(authToChooseUrl.getClass());
            if (url != null) {
                // mapping matched, so restoring authentication...
                // TODO: is this correct? it's explicitly cleared before...
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        if (url == null) {
            url = getLoginFormUrl();
        }
        return url;
    }

}
