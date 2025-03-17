package com.payneteasy.superfly.web.spring.dao;

import com.googlecode.jdbcproc.daofactory.DaoMethodInfoFactory;
import com.googlecode.jdbcproc.daofactory.StoredProcedureDaoFactoryBean;
import com.googlecode.jdbcproc.daofactory.impl.dbstrategy.impl.CallableStatementGetStrategyFactoryIndexImpl;
import com.googlecode.jdbcproc.daofactory.impl.dbstrategy.impl.CallableStatementSetStrategyFactoryIndexImpl;
import com.googlecode.jdbcproc.daofactory.impl.parameterconverter.ParameterConverterServiceImpl;
import com.googlecode.jdbcproc.daofactory.impl.procedureinfo.StoredProcedureInfoManagerInitOnStartup;
import com.payneteasy.superfly.dao.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * Spring Context configuration for UI DAO beans.
 *
 * @author rpuch
 */
@Slf4j
@Configuration
public class SpringUIDaoConfiguration {
    @Resource(name = "uiDataSource")
    private DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
    }

    // transaction annotations consuming infrastructure
    @Bean
    public TransactionAnnotationParser transactionAnnotationParser() {
        return new SpringTransactionAnnotationParser();
    }

    @Bean
    public TransactionAttributeSource transactionAttributeSource() {
        return new AnnotationTransactionAttributeSource(transactionAnnotationParser());
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        // by name, so if a qualifier is specified in @Transactional,
        // correct transaction manager is selected
        interceptor.setTransactionManagerBeanName("transactionManager");
        interceptor.setTransactionAttributeSource(transactionAttributeSource());
        return interceptor;
    }

    @Bean
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAttributeSourceAdvisor() {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource());
        advisor.setAdviceBeanName("transactionInterceptor");
        return advisor;
    }

    // gear for main datasource's procedures

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return createJdbcTemplate(dataSource);
    }

    private DaoMethodInfoFactory createDaoMethodInfoFactoryTemplate() {
        DaoMethodInfoFactory bean = new DaoMethodInfoFactory();
        bean.setCallableStatementGetStrategyFactory(new CallableStatementGetStrategyFactoryIndexImpl());
        bean.setCallableStatementSetStrategyFactory(new CallableStatementSetStrategyFactoryIndexImpl());
        bean.setParameterConverterService(new ParameterConverterServiceImpl());
        return bean;
    }

    @Bean
    public StoredProcedureInfoManagerInitOnStartup storedProcedureInfoManager() throws Exception {
        return new StoredProcedureInfoManagerInitOnStartup(jdbcTemplate());
    }

    @Bean
    public DaoMethodInfoFactory daoMethodInfoFactory() throws Exception {
        DaoMethodInfoFactory bean = createDaoMethodInfoFactoryTemplate();
        bean.setJdbcTemplate(jdbcTemplate());
        bean.setStoredProcedureInfoManager(storedProcedureInfoManager());
        return bean;
    }


    @Bean
    public ActionDao getActionDao() {
        return createDao(ActionDao.class);
    }

    @Bean
    public GroupDao getGroupDao() {
        return createDao(GroupDao.class);
    }

    @Bean
    public RoleDao getRoleDao() {
        return createDao(RoleDao.class);
    }

    @Bean
    public SessionDao getSessionDao() {
        return createDao(SessionDao.class);
    }

    @Bean
    public SmtpServerDao getSmtpServerDao() {
        return createDao(SmtpServerDao.class);
    }

    @Bean
    public SubsystemDao getSubsystemDao() {
        return createDao(SubsystemDao.class);
    }

    @Bean
    public UserDao getUserDao() {
        return createDao(UserDao.class);
    }

    private <T> T createDao(Class<T> clazz) {
        StoredProcedureDaoFactoryBean factoryBean = new StoredProcedureDaoFactoryBean();
        factoryBean.setJdbcTemplate(jdbcTemplate());
        try {
            factoryBean.setDaoMethodInfoFactory(daoMethodInfoFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        factoryBean.setInterface(clazz);
        try {
            // suppressing unchecked warning because we are expecting
            // T as the returned value
            @SuppressWarnings("unchecked") T result = (T) factoryBean.getObject();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate a bean", e);
        }
    }


    private JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
