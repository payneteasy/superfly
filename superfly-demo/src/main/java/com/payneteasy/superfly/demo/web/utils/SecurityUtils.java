package com.payneteasy.superfly.demo.web.utils;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.annotation.Secured;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

import com.payneteasy.superfly.security_2_0.authentication.FastAuthentication;

public class SecurityUtils {
    public static boolean isUserInRole(String aRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication) {
            return false;
        }
        
        GrantedAuthority[] authorities = authentication.getAuthorities();
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
            return null;
        }
        GrantedAuthority[] authorities = authentication.getAuthorities();
        if (authentication.getPrincipal() == null || authorities == null) {
            return null;
        }

        String[] roles = new String[authorities.length];
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
            if (roles != null && roles.length > 0) {
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
