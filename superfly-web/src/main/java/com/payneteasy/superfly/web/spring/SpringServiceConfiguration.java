package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.common.SuperflyProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
@ComponentScan(basePackages = "com.payneteasy.superfly")
public class SpringServiceConfiguration {
    private final SuperflyProperties properties;

    @Bean
    public CryptoService cryptoService() {
        return new CryptoServiceImpl(
                properties.cryptoSecret(),
                properties.cryptoSalt()
        );
    }
}
