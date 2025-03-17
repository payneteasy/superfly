package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.crypto.pgp.PGPCrypto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCryptoConfiguration {
    @Bean
    public PublicKeyCrypto publicKeyCrypto() {
        return new PGPCrypto();
    }
}
