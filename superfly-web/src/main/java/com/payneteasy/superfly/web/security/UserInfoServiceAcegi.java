package com.payneteasy.superfly.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.service.UserInfoService;
import org.springframework.stereotype.Service;

/**
 * Uses Spring Security {@link Authentication} to get current user.
 *
 * @author Roman Puchkovskiy
 */
@Service
public class UserInfoServiceAcegi implements UserInfoService {

    public String getUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
