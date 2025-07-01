package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for API serialization.
 */
@Configuration
public class ApiSerializationConfiguration {

    /**
     * Creates and configures ApiSerializationManager as a Spring bean.
     *
     * @return Configured ApiSerializationManager
     */
    @Bean
    public ApiSerializationManager apiSerializationManager() {
        return new ApiSerializationManager();
    }
}
