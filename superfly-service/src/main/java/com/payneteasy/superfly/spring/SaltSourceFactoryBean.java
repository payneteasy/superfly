package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.SaltSource;

import java.util.Map;

/**
 * {@link FactoryBean} for {@link SaltSource}. Produces it according to the
 * current policy value.
 * 
 * @author Roman Puchkovskiy
 */
public class SaltSourceFactoryBean extends AbstractPolicyDependingFactoryBean {
	
	private SaltSource source;
	private String data = "data";

    private Map<String,SaltSource> salts;

    public void setSalts(Map<String, SaltSource> salts) {
        this.salts = salts;
    }

    public void setData(String data) {
		this.data = data;
	}

	public Object getObject() throws Exception {
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
