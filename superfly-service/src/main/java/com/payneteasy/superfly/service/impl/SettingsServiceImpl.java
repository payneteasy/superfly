package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.spring.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author rpuch
 */
public class SettingsServiceImpl implements SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsServiceImpl.class);

    private Policy policy;
    private String superflyVersion;

    public SettingsServiceImpl() {
        superflyVersion = initSuperflyVersion(AuthSession.class,
                "/META-INF/maven/com.payneteasy.superfly/superfly-service/pom.properties");
    }


    @Required
    public void setPolicyName(String name) {
        this.policy = Policy.valueOf(name.toUpperCase());
    }

    @Override
    public Policy getPolicy() {
        return policy;
    }

    @Override
    public String getSuperflyVersion() {
        return superflyVersion;
    }

    private String initSuperflyVersion(Class<?> clazz, String pomLocation) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HHmm");
        String startupTime = dateFormat.format(new Date());

        InputStream in = clazz.getResourceAsStream(pomLocation);
        if (in != null) {
            try {
                try {
                    Properties props = new Properties();
                    props.load(in);
                    String version = props.getProperty("version");
                    if (version == null) {
                        version = "version-not-found-started-" + startupTime;
                    } else if (version.endsWith("SNAPSHOT")) {
                        version = version + "-started-" + startupTime;
                    }
                    return version;
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                logger.warn("Cannon get superfly version: {}", e.getMessage());
                return "version";
            }

        } else {
            return "unknown-started-" + dateFormat.format(new Date());
        }
    }

}
