package com.payneteasy.superfly.web.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

/**
 * UserDetails implementation used in Superfly locally by
 * SuperflyLocalAuthenticationProvider.
 * 
 * @author Roman Puchkovskiy
 */
public class UserDetailsImpl implements UserDetails {
	
	private String username;
	private String password;
	private GrantedAuthority[] authorities;

	public UserDetailsImpl(String username, String password, String[] actions,
			String rolePrefix) {
		super();
		this.username = username;
		this.password = password;
		this.authorities = new GrantedAuthority[actions.length];
		for (int i = 0; i < actions.length; i++) {
			authorities[i] = new GrantedAuthorityImpl(rolePrefix + actions[i].toUpperCase());
		}
	}

	public GrantedAuthority[] getAuthorities() {
		return authorities;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

}
