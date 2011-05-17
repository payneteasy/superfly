package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * Base for factory beans which are aware of policies.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractPolicyDependingFactoryBean<T> implements FactoryBean<T> {
	
	private String policyName;
	
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	protected Policy findPolicyByIdentifier() {
		Policy p = null;
		for (Policy policy : Policy.values()) {
			if (policy.getIdentifier().equals(policyName)) {
				p = policy;
				break;
			}
		}
		if (p == null) {
			throw new IllegalArgumentException();
		}
		return p;
	}

}