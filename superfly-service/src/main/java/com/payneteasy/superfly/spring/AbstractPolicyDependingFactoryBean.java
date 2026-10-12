package com.payneteasy.superfly.spring;

import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Base for factory beans which are aware of policies.
 *
 * @author Roman Puchkovskiy
 */
@Setter
public abstract class AbstractPolicyDependingFactoryBean<T> implements FactoryBean<T>, EnvironmentAware {

    // When not set, the policy of the application is used (see PolicyParameter)
    private String policyName;

    private Environment environment;

    protected Policy findPolicyByIdentifier() {
        return PolicyParameter.parse(policyName != null ? policyName : PolicyParameter.rawValue(environment));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
