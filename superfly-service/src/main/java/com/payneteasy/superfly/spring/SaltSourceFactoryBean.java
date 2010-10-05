package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.SaltSource;

/**
 * {@link FactoryBean} for {@link SaltSource}. Produces it according to the
 * current policy value.
 * 
 * @author Roman Puchkovskiy
 */
public class SaltSourceFactoryBean extends AbstractPolicyDependingFactoryBean {
	
	private SaltSource source;
	private String data = "data";

	public void setData(String data) {
		this.data = data;
	}

	public Object getObject() throws Exception {
		if (source == null) {
			Policy p = findPolicyByIdentifier();
			switch (p) {
			case NONE:
				source = new NullSaltSource();
				break;
			case PCIDSS:
				source = new ConstantSaltSource(data);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		return source;
	}

	public Class<?> getObjectType() {
		return SaltSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
