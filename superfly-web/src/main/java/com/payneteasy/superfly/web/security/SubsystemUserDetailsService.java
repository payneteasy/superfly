package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.security.exception.SubsystemNotAllowedHostException;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * UserDetailsService implementation used to authenticate subsystems.
 *
 * @author Roman Puchkovskiy
 */
@Service
public class SubsystemUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(SubsystemUserDetailsService.class);

    private final SubsystemService subsystemService;
    private final LoggerSink       loggerSink;

    @Setter
    private String[] authorities = {"ROLE_SUBSYSTEM"};

    public SubsystemUserDetailsService(SubsystemService subsystemService, LoggerSink loggerSink) {
        this.subsystemService = subsystemService;
        this.loggerSink = loggerSink;
    }

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        UISubsystem subsystem = subsystemService.getSubsystemByName(username);
        if (subsystem == null) {
            loggerSink.info(logger, "CHECK_SUBSYSTEM_EXIST", false, username);
            throw new SubsystemNotAllowedHostException(
                    String.format("Subsystem %s (from CN=%s) not found in subsystems. Please check your Issue.CN field", username, username)
            );
        }
        GrantedAuthority[] gas = new GrantedAuthority[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            gas[i] = new SimpleGrantedAuthority(authorities[i]);
        }
        loggerSink.info(logger, "CHECK_SUBSYSTEM_EXIST", true, username);
        return new User(username, subsystem.getSubsystemToken(), true, true, true, true, Arrays.asList(gas));
    }

}
