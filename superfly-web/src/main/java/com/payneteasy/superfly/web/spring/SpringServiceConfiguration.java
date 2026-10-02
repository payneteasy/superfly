package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.hotp.HOTPProviderUtils;
import com.payneteasy.superfly.hotp.NullHOTPProvider;
import com.payneteasy.superfly.spi.HOTPProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@AllArgsConstructor
@Configuration
// web.mvc belongs to the rest-api servlet context (dispatcher-servlet.xml); in root it would
// also be mapped under the remoting servlet.
@ComponentScan(basePackages = "com.payneteasy.superfly",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.payneteasy\\.superfly\\.web\\.mvc\\..*"))
public class SpringServiceConfiguration {
    private final SuperflyProperties properties;

    @Bean
    public CryptoService cryptoService() {
        CryptoServiceImpl.requireConfigured(
                "SUPERFLY_CRYPTO_SECRET", properties.cryptoSecret(),
                "SUPERFLY_CRYPTO_SALT", properties.cryptoSalt()
        );
        return new CryptoServiceImpl(
                properties.cryptoSecret(),
                properties.cryptoSalt(),
                Boolean.TRUE.equals(properties.cryptoLegacyDefaultKey())
        );
    }

    @Bean
    public HOTPProvider hotpProvider() {
        HOTPProvider provider = HOTPProviderUtils.instantiateProvider(false);
        return provider != null ? provider : new NullHOTPProvider();
    }
}
