package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;

/**
 * UserDetailsService implementation used to authenticate subsystems.
 * 
 * @author Roman Puchkovskiy
 */
public class SubsystemUserDetailsService implements UserDetailsService {

    private SubsystemService subsystemService;
    private String[] authorities = {"ROLE_SUBSYSTEM"};

    @Required
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }

    public void setAuthorities(String[] authorities) {
        this.authorities = authorities;
    }

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        UISubsystem subsystem = subsystemService.getSubsystemByName(username);
        if (subsystem == null) {
            throw new UsernameNotFoundException("No subsystem was found with name=" + username);
        }
        GrantedAuthority[] gas = new GrantedAuthority[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            gas[i] = new SimpleGrantedAuthority(authorities[i]);
        }
        return new User(username, "NOT-USED", true, true, true, true, Arrays.asList(gas));
    }

}
