/**
 * 
 */
package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.service.UserInfoService;

class UserInfoServiceMock implements UserInfoService {
	public String getUsername() {
		return "test-user";
	}
}