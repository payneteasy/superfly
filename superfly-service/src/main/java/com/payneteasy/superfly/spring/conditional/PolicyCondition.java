package com.payneteasy.superfly.spring.conditional;

import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.PolicyParameter;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PolicyCondition implements Condition {

    public static final String SUPERFLY_POLICY = PolicyParameter.NAME;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Policy activePolicy = findPolicyByIdentifier(PolicyParameter.rawValue(context.getEnvironment()));
        Policy[] policy = (Policy[]) metadata.getAnnotationAttributes(OnPolicyCondition.class.getName()).get("value");

        for (Policy thePolicy : policy) {
            if (thePolicy == activePolicy) return true;
        }
        return false;
    }

    protected Policy findPolicyByIdentifier(String policyName) {
        return PolicyParameter.parse(policyName);
    }
}
