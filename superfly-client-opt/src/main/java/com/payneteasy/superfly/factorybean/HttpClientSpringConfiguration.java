package com.payneteasy.superfly.factorybean;

import lombok.AllArgsConstructor;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class HttpClientSpringConfiguration {

    @Bean
    public HttpClientFactoryBean httpClientFactoryBean() {
        return new HttpClientFactoryBean()
                .httpConnectionManager(new MultiThreadedHttpConnectionManager())
                .connectionManagerTimeout(10_000)
                .soTimeout(30_000)
                .connectionTimeout(30_000)
                .sslEnabledProtocols(new String[]{"SSLv3"});
    }
}
