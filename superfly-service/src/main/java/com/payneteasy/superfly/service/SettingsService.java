package com.payneteasy.superfly.service;

import com.payneteasy.superfly.spring.Policy;

/**
 * @author rpuch
 */
public interface SettingsService {

    Policy getPolicy();

    String getSuperflyVersion();
}
