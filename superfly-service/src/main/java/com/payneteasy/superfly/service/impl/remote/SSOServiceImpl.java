package com.payneteasy.superfly.service.impl.remote;

import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;

/**
 * Implementation of SSOService.
 * 
 * @author Roman Puchkovskiy
 */
@Transactional
public class SSOServiceImpl implements SSOService {

	/**
	 * @see SSOService#authenticate(String, String)
	 */
	public SSOUser authenticate(String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
