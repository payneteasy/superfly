package com.payneteasy.superfly.service;

public interface LocalSecurityService {
	String[] authenticate(String username, String password);
}
