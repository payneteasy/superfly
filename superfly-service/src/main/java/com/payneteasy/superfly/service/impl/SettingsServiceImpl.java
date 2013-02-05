package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.spring.Policy;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author rpuch
 */
public class SettingsServiceImpl implements SettingsService {
    private boolean hotpDisabled;
    private Policy policy;

    @Required
    public void setHotpDisabled(boolean hotpDisabled) {
        this.hotpDisabled = hotpDisabled;
    }

    @Required
    public void setPolicyName(String name) {
        this.policy = Policy.valueOf(name.toUpperCase());
    }

    @Override
    public boolean isHotpDisabled() {
        return hotpDisabled;
    }

    @Override
    public Policy getPolicy() {
        return policy;
    }
}
