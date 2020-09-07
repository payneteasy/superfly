package com.payneteasy.superfly.demo.web.utils;

import com.payneteasy.superfly.security.authentication.FastAuthentication;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class SecurityUtils {
    public static boolean isUserInRole(String aRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authentication.getPrincipal() == null || authorities == null) {
            return false;
        }
        
        if (authentication instanceof FastAuthentication) {
            FastAuthentication fastAuthentication = (FastAuthentication) authentication;
            if (fastAuthentication.hasAuthority(aRole)) {
                return true;
            }
        } else {
            for (GrantedAuthority authority : authorities) {
                if (aRole.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String[] getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication) {
            return new String[0];
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authentication.getPrincipal() == null || authorities == null) {
            return new String[0];
        }

        String[] roles = new String[authorities.size()];
        int i = 0;
        for (GrantedAuthority authority : authorities) {
            roles[i] = authority.getAuthority();
            i++;
        }

        return roles;
    }
    
    public static String getUsername() {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (obj instanceof UserDetails) {
            username = ((UserDetails) obj).getUsername();
        } else {
            username = obj.toString();
        }
        return username;
    }

    @SuppressWarnings("unchecked")
    public static boolean isComponentVisible(Class aComponentClass) {
        if (aComponentClass.isAnnotationPresent(Secured.class)) {
            Secured securedAnnotation = (Secured) aComponentClass.getAnnotation(Secured.class);
            String[] roles = securedAnnotation.value();
            if (roles.length > 0) {
                for (String role : roles) {
                    if (isUserInRole(role)) {
                        return true;
                    }
                }
                return false;
            } else {
                throw new RuntimeException("Page " + aComponentClass.getName() + " has no roles in Secured annotation");
            }

        } else {
            throw new RuntimeException("Page " + aComponentClass.getName() + " has no Secured annotation");
        }
    }
}
