package com.payneteasy.superfly.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

/**
 * Filter which is used to show a dropdown from which user selects a role with
 * which they want to log in to the system.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflySelectRoleAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private String roleParameter = "j_role";

    protected SuperflySelectRoleAuthenticationProcessingFilter() {
        super("/j_superfly_select_role");
    }

    public void setRoleParameter(String roleParameter) {
        this.roleParameter = roleParameter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException,
            IOException, ServletException {
        String roleKey = obtainRoleKey(request);
        SSOUser ssoUser = (SSOUser) request.getSession().getAttribute(
                SSOUserTransportAuthenticationToken.SESSION_KEY);
        request.getSession().removeAttribute(
                SSOUserTransportAuthenticationToken.SESSION_KEY);
        if (ssoUser == null) {
            throw new BadCredentialsException("Session expired");
        }
        SSORole selectedRole = null;
        for (SSORole role : ssoUser.getActionsMap().keySet()) {
            if (role.getName().equals(roleKey)) {
                selectedRole = role;
                break;
            }
        }
        if (selectedRole == null) {
            throw new BadCredentialsException("Unknown role: " + roleKey);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CompoundAuthentication compound = getCompoundAuthenticationOrNewOne(authentication);

        Authentication authRequest = createUserRoleAuthRequest(ssoUser, selectedRole);
        return getAuthenticationManager().authenticate(new CompoundAuthentication(compound.getReadyAuthentications(), authRequest));
    }

    protected String obtainRoleKey(HttpServletRequest request) {
        return request.getParameter(roleParameter);
    }

    protected Authentication createUserRoleAuthRequest(SSOUser ssoUser, SSORole role) {
        return new SSOUserAndSelectedRoleAuthenticationToken(ssoUser, role);
    }

}
