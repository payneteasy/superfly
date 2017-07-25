package com.payneteasy.superfly.security.spring.internal;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SecuredMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(SecuredMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation aMethodInvocation) throws Throwable {
        checkSecurityActions(aMethodInvocation.getMethod());
        return aMethodInvocation.proceed();
    }

    private void checkSecurityActions(Method aMethod) {
        Secured securedAnnotation = aMethod.getAnnotation(Secured.class);
        if(securedAnnotation == null) {
            throw new IllegalStateException("No @Secured annotation for method " + aMethod);
        }
        SecurityContext context = SecurityContextStore.findInThreadLocal();
        for (String secureAction : securedAnnotation.value()) {
            if(context.hasSecureAction(secureAction)) {
                return;
            }
        }

        String message = "There are no any " + Arrays.asList(securedAnnotation.value()) + " for user " + context.getUsername();
        LOG.error(message);

        throw new IllegalStateException(message);
    }
}
