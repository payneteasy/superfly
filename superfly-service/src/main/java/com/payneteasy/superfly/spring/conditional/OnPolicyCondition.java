package com.payneteasy.superfly.spring.conditional;

import com.payneteasy.superfly.spring.Policy;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(PolicyCondition.class)
public @interface OnPolicyCondition {
    Policy[] value() default {Policy.NONE};
}
