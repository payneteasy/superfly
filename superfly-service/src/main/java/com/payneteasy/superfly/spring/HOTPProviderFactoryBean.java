package com.payneteasy.superfly.spring;

import java.util.ServiceLoader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;

import com.payneteasy.superfly.hotp.NullHOTPProvider;
import com.payneteasy.superfly.spi.HOTPProvider;

/**
 * {@link FactoryBean} for {@link HOTPProvider}. It first tries to instantiate
 * an implementation using a Java Service infrastructure (/META-INF/service).
 * If no providers found, it instantiates a default one.
 *
 * @author Roman Puchkovskiy
 */
public class HOTPProviderFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {
	
	private BeanFactory beanFactory;
	private HOTPProvider hotpProvider;
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
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
		Assert.notNull(beanFactory);
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory);
		ServiceLoader<HOTPProvider> loader = ServiceLoader.load(HOTPProvider.class);
		if (loader.iterator().hasNext()) {
			hotpProvider = loader.iterator().next();
		} else {
			hotpProvider = new NullHOTPProvider();
		}
		hotpProvider.init(new BeanFactoryObjectResolver((ListableBeanFactory) beanFactory));
	}

}
