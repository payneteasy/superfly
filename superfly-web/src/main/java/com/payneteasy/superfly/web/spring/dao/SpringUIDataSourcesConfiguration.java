package com.payneteasy.superfly.web.spring.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Defines data-sources for UI application.
 *
 * @author rpuch
 */
@Slf4j
public class SpringUIDataSourcesConfiguration {

    public static final String JAVA_COMP_ENV_JDBC_SUPERFLY = "java:comp/env/jdbc/superfly";

    @Bean
    public DataSource uiDataSource() {
        try {
            JndiTemplate jndiTemplate = new JndiTemplate();
            DataSource   dataSource   = jndiTemplate.lookup(JAVA_COMP_ENV_JDBC_SUPERFLY, DataSource.class);
            log.info("DataSource successfully init from JNDI: {}",  JAVA_COMP_ENV_JDBC_SUPERFLY);
            return dataSource;
        } catch (NamingException e) {
            throw new RuntimeException("JNDI lookup failed", e);
        }
    }
}
