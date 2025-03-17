package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.spring.Policy;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class SettingsServiceImpl implements SettingsService {
    private final SuperflyProperties properties;

    public SettingsServiceImpl(SuperflyProperties properties) {
        this.properties = properties;
    }

    @Override
    public Policy getPolicy() {
        return properties.policyName() == null ? Policy.NONE : Policy.valueOf(properties.policyName());
    }

    @Override
    public String getSuperflyVersion() {
        return properties.superflyVersion();
    }
}
