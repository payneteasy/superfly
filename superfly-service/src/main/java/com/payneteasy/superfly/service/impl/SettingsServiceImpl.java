package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.spring.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Slf4j
@Service
public class SettingsServiceImpl implements SettingsService {
    private final SuperflyProperties properties;

    public SettingsServiceImpl(SuperflyProperties properties) {
        this.properties = properties;
        log.debug("properties {}", properties);
    }

    @Override
    public Policy getPolicy() {
        if (properties != null && properties.policyName() == null) {
            return Policy.NONE;
        } else {
            assert properties != null;
            return Policy.fromIdentifier(properties.policyName());
        }
    }

    @Override
    public String getSuperflyVersion() {
        return properties.superflyVersion();
    }
}
