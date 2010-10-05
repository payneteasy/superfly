package com.payneteasy.superfly.web.security;

import org.springframework.security.context.SecurityContextHolder;

import com.payneteasy.superfly.service.UserInfoService;

public class UserInfoServiceAcegi implements UserInfoService {

	public String getUsername() {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return null;
		}
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
