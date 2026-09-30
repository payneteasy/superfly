package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

public interface RoleSource {
    String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole);
}
