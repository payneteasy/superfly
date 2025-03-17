package com.payneteasy.superfly.spring.conditional;

import com.payneteasy.superfly.spring.Policy;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PolicyCondition implements Condition {

    public static final String SUPERFLY_POLICY = "superfly-policy";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        Policy policeNameFromWebXml = findPolicyByIdentifier(
                env.getProperty(
                        SUPERFLY_POLICY,
                        env.getProperty(SUPERFLY_POLICY, Policy.NONE.getIdentifier())
                )
        );
        Policy[] policy = (Policy[]) metadata.getAnnotationAttributes(OnPolicyCondition.class.getName()).get("value");

        for (Policy thePolicy : policy) {
            if (thePolicy == policeNameFromWebXml) return true;
        }
        return false;
    }

    protected Policy findPolicyByIdentifier(String policyName) {
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
