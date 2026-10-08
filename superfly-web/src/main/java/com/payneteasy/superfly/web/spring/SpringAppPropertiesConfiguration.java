package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.AuthSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Configuration
public class SpringAppPropertiesConfiguration {
    private static final String POM_PROPERTIES = "/META-INF/maven/com.payneteasy.superfly/superfly-web/pom.properties";

    private final ApplicationParameterResolver parameterResolver;

    public SpringAppPropertiesConfiguration(ApplicationParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @Bean
    public SuperflyProperties superflyProperties() {
        String policyName = policyName();
        log.info("POLICY NAME: {}", policyName);
        return new SuperflyProperties()
                .superflyVersion(superflyVersion())
                .policyName(policyName)
                .cryptoSecret(cryptoSecret())
                .cryptoSalt(cryptoSalt())
                .cryptoLegacyDefaultKey(cryptoLegacyDefaultKey())
                .maxLoginsFailed(maxLoginsFailed())
                .csrfLoginValidatorEnable(csrfLoginValidatorEnable())
                .enableMultiFactorAuth(enableMultiFactorAuth())
                .forceMultiFactorAuthMethod(forceMultiFactorAuthMethod())
                .loginIpLimit(loginIpLimit());
    }

    private String superflyVersion() {
        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy.MM.dd-HHmm");
        String           startupTime = dateFormat.format(new Date());

        InputStream in = getClass().getResourceAsStream(POM_PROPERTIES);
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
                log.warn("Cannon get superfly version: {}", e.getMessage());
                return "version";
            }

        } else {
            return "unknown-started-" + dateFormat.format(new Date());
        }
    }

    private String policyName() {
        return parameterResolver.getParameter("superfly-policy", "none");
    }

    private String cryptoSecret() {
        return envOrParameter("SUPERFLY_CRYPTO_SECRET", "superfly-cryptoSecret");
    }

    private String cryptoSalt() {
        return envOrParameter("SUPERFLY_CRYPTO_SALT", "superfly-cryptoSalt");
    }

    private boolean cryptoLegacyDefaultKey() {
        return Boolean.parseBoolean(envOrParameter("SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY", "superfly-cryptoLegacyDefaultKey"));
    }

    // No default for the key material: a missing value must fail the start (see SpringServiceConfiguration#cryptoService)
    private String envOrParameter(String envName, String paramName) {
        String fromEnv = System.getenv(envName);
        return fromEnv != null && !fromEnv.isBlank() ? fromEnv : parameterResolver.getParameter(paramName, null);
    }

    // Failed logins per IP per window; 0 disables the IP limit (the IP + username limit stays)
    private int loginIpLimit() {
        String value = envOrParameter("SUPERFLY_LOGIN_IP_LIMIT", "superfly-loginIpLimit");
        return value == null ? 20 : Integer.parseInt(value.trim());
    }

    private OTPType forceMultiFactorAuthMethod() {
        return OTPType.fromCode(parameterResolver.getParameter("superfly-mfa-method-force", "none"));
    }

    private Long maxLoginsFailed() {
        return Long.parseLong(parameterResolver.getParameter("superfly-max-logins-failed", "6"));
    }

    private Boolean csrfLoginValidatorEnable() {
        return Boolean.parseBoolean(parameterResolver.getParameter("superfly-csrf-login-validator-enable", "true"));
    }

    private Boolean enableMultiFactorAuth() {
        return Boolean.parseBoolean(parameterResolver.getParameter("superfly-mfa-enabled", "true"));
    }
}
