package com.payneteasy.superfly.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import com.payneteasy.superfly.hotp.HOTPProviderContextImpl;
import com.payneteasy.superfly.hotp.HOTPProviderUtils;
import com.payneteasy.superfly.hotp.NullHOTPProvider;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;
import com.payneteasy.superfly.spisupport.ObjectResolver;

/**
 * {@link FactoryBean} for {@link HOTPProvider}. It first tries to instantiate
 * an implementation using a Java Service infrastructure (/META-INF/service).
 * If no providers found, it instantiates a default one.
 *
 * @author Roman Puchkovskiy
 */
public class HOTPProviderFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HOTPProviderFactoryBean.class);

    private BeanFactory beanFactory;

    private String masterKey;
    private int codeDigits = 6;
    private int lookahead = 10;
    private int tableSize = 100;

    private boolean allowTestProvider = false;

    private HOTPProvider hotpProvider;

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Required
    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public void setCodeDigits(int codeDigits) {
        this.codeDigits = codeDigits;
    }

    public void setLookahead(int lookahead) {
        this.lookahead = lookahead;
    }

    public void setTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    public void setAllowTestProvider(boolean allowTestProvider) {
        this.allowTestProvider = allowTestProvider;
    }

    public Object getObject() throws Exception {
        return hotpProvider;
    }

    public Class<?> getObjectType() {
        return HOTPProvider.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(beanFactory, "beanFactory is null");
        Assert.isInstanceOf(ListableBeanFactory.class, beanFactory);

        HOTPProvider resultProvider = HOTPProviderUtils.instantiateProvider(allowTestProvider);

        if (resultProvider != null) {
            logger.info("Found the following implementation via service loader: " + resultProvider.getClass().getName());
        } else {
            resultProvider = new NullHOTPProvider();
            logger.info("Did not find an implementation via service loader. Falling back to a default implementation");
        }
        hotpProvider = resultProvider;

        ObjectResolver objectResolver = createObjectResolver();
        HOTPProviderContext context = createHOTPProviderContext(objectResolver);
        hotpProvider.init(context);
    }

    protected HOTPProviderContext createHOTPProviderContext(
            ObjectResolver objectResolver) {
        HOTPProviderContext context = new HOTPProviderContextImpl(objectResolver, masterKey, codeDigits, lookahead, tableSize);
        return context;
    }

    protected ObjectResolver createObjectResolver() {
        return new BeanFactoryObjectResolver((ListableBeanFactory) beanFactory);
    }

}
