package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.spring.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class SpringAppPropertiesConfiguration {
    private static final String POM_PROPERTIES = "/META-INF/maven/com.payneteasy.superfly/superfly-web/pom.properties";
    private static final int MAX_VALUE_IN_ERROR = 64;

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
                .maxOtpFailed(maxOtpFailed())
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
        return choiceParameter("superfly-policy", Policy.NONE, Policy.values(), Policy::getIdentifier).getIdentifier();
    }

    private String cryptoSecret() {
        return envOrParameter("SUPERFLY_CRYPTO_SECRET", "superfly-cryptoSecret");
    }

    private String cryptoSalt() {
        return envOrParameter("SUPERFLY_CRYPTO_SALT", "superfly-cryptoSalt");
    }

    private boolean cryptoLegacyDefaultKey() {
        return strictBoolean("SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY/superfly-cryptoLegacyDefaultKey",
                envOrParameter("SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY", "superfly-cryptoLegacyDefaultKey"), false);
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
        return choiceParameter("superfly-mfa-method-force", OTPType.NONE, OTPType.values(), OTPType::code);
    }

    private Long maxLoginsFailed() {
        return Long.parseLong(parameterResolver.getParameter("superfly-max-logins-failed", "6"));
    }

    // Falls back to superfly-max-logins-failed when not set
    private Long maxOtpFailed() {
        String value = parameterResolver.getParameter("superfly-max-otp-failed", null);
        return value == null || value.isBlank() ? maxLoginsFailed() : Long.parseLong(value.trim());
    }

    private Boolean csrfLoginValidatorEnable() {
        return booleanParameter("superfly-csrf-login-validator-enable", true);
    }

    private Boolean enableMultiFactorAuth() {
        return booleanParameter("superfly-mfa-enabled", true);
    }

    // A security switch must not change on a typo: only true/false are accepted (any case, spaces around are ignored),
    // a blank value is the same as no value (the default), anything else stops the start.
    private boolean booleanParameter(String name, boolean defaultValue) {
        return strictBoolean(name, parameterResolver.getParameter(name, null), defaultValue);
    }

    private static boolean strictBoolean(String name, String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String trimmed = value.strip();
        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }
        throw invalidValue(name, value, "true, false");
    }

    // Same for a parameter with a fixed set of codes: an unknown code stops the start instead of becoming a default.
    private <E> E choiceParameter(String name, E defaultValue, E[] choices, Function<E, String> code) {
        String value = parameterResolver.getParameter(name, null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String trimmed = value.strip();
        for (E choice : choices) {
            if (code.apply(choice).equalsIgnoreCase(trimmed)) {
                return choice;
            }
        }
        throw invalidValue(name, value, Arrays.stream(choices).map(code).collect(Collectors.joining(", ")));
    }

    private static IllegalArgumentException invalidValue(String name, String value, String allowed) {
        String shown = value.length() > MAX_VALUE_IN_ERROR ? value.substring(0, MAX_VALUE_IN_ERROR) + "..." : value;
        return new IllegalArgumentException(
                "Parameter '" + name + "' has invalid value '" + shown + "', allowed values: " + allowed);
    }
}
