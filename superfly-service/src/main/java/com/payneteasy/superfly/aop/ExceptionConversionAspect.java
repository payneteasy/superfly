package com.payneteasy.superfly.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.payneteasy.superfly.utils.CloneExceptionUtils;

/**
 * Aspect which converts a thrown exception (including all chained exceptions)
 * to classes that are guaranteed to exist on any Java VM, so such a converted
 * exception will not cause a ClassNotFoundError when deserializing anywhere.
 * It's meant to be around any services which are remotely accessible.
 * It should be defined BEFORE any aspects which wrap exceptions to their
 * specific exceptions.
 * 
 * @author Roman Puchkovskiy
 */
@SuppressWarnings("unchecked")
@Aspect
public class ExceptionConversionAspect {

    private Class<? extends Exception>[] nonConvertibleClasses = new Class[]{};

    public void setNonConvertibleClasses(
            Class<? extends Exception>[] nonConvertibleClasses) {
        this.nonConvertibleClasses = nonConvertibleClasses;
    }

    @Around("execution(* com.payneteasy.superfly.service.impl.remote.SSOServiceImpl.*(..))")
    public Object invoke(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return proceedingJoinPoint.proceed();
        } catch (Exception ex) {
            throw CloneExceptionUtils.cloneException(ex, nonConvertibleClasses);
        }
    }
}