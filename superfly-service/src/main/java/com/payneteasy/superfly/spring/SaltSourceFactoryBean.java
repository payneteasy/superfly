package com.payneteasy.superfly.spring;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.superfly.password.SaltSource;

/**
 * {@link FactoryBean} for {@link SaltSource}. Produces it according to the
 * current policy value.
 * 
 * @author Roman Puchkovskiy
 */
public class SaltSourceFactoryBean extends AbstractPolicyDependingFactoryBean<SaltSource> {
	
	private SaltSource source;

    private Map<String,SaltSource> salts;

    public void setSalts(Map<String, SaltSource> salts) {
        this.salts = salts;
    }

	public SaltSource getObject() throws Exception {
		if (source == null) {
			Policy p = findPolicyByIdentifier();
            if(salts.containsKey(p.name().toLowerCase())){
				source = salts.get(p.name().toLowerCase());
			} else
                throw new IllegalArgumentException();
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
