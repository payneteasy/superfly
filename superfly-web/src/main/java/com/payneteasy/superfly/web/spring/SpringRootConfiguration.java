package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.web.spring.dao.SpringUIDaoConfiguration;
import com.payneteasy.superfly.web.spring.dao.SpringUIDataSourcesConfiguration;
import com.payneteasy.superfly.web.spring.security.SpringSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import({
        SpringAppPropertiesConfiguration.class,
        SpringUIDataSourcesConfiguration.class,
        SpringUIDaoConfiguration.class,
        SpringServiceConfiguration.class,
        WebConfig.class,
        SpringSecurityConfiguration.class,
        AopConfig.class,
        QuartzConfig.class,
})
@EnableTransactionManagement
public class SpringRootConfiguration {

}
