package com.payneteasy.superfly.security.spring;

import com.payneteasy.superfly.security.spring.internal.SecuredMethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.annotation.Secured;

import java.lang.reflect.Method;
import java.util.*;

import static org.springframework.aop.support.AopUtils.canApply;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.aop.support.annotation.AnnotationMatchingPointcut.forMethodAnnotation;
import static org.springframework.util.ClassUtils.getDefaultClassLoader;

public class SecuredBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SecuredBeanPostProcessor.class);

    private final ProxyConfig proxyConfig;
    private final Pointcut    pointcut;
    private final ClassLoader beanClassLoader;

    private static final Set<String> ACTIONS = new HashSet<>();

    public static String[] getCollectedActions() {
        if(ACTIONS.isEmpty()) {
            throw new IllegalStateException("No actions was collected. Possible reasons: no SecuredBeanPostProcessor configured or no methods with @Secured annotation.");
        }
        String[] array = new String[ACTIONS.size()];
        ACTIONS.toArray(array);
        return array;
    }

    public SecuredBeanPostProcessor() {
        proxyConfig     = new ProxyConfig();
        pointcut        = forMethodAnnotation(Secured.class);
        beanClassLoader = getDefaultClassLoader();
    }

    @Override
    public Object postProcessBeforeInitialization(Object aBean, String aBeanName) throws BeansException {
        return aBean;
    }

    @Override
    public Object postProcessAfterInitialization(Object aBean, String aBeanName) throws BeansException {
        Class<?> clazz = getTargetClass(aBean);
        if(canApply(pointcut, clazz)) {
            if(aBean instanceof Advised) {
                LOG.info("Advised: {}", clazz.getSimpleName());
            } else {
                addActions(clazz);
                return createProxyFactory(aBean, clazz);
            }
        }
        return aBean;
    }

    private void addActions(Class<?> aClass) {
        addFromMethodsList(aClass.getMethods());
        for (Class<?> clazz : aClass.getInterfaces()) {
            addFromMethodsList(clazz.getMethods());
        }
    }

    private void addFromMethodsList(Method[] methods) {
        for (Method method : methods) {
            if(method.isAnnotationPresent(Secured.class)) {
                Secured secureAnnotation = method.getAnnotation(Secured.class);
                Collections.addAll(ACTIONS, secureAnnotation.value());
            }
        }
    }

    private Object createProxyFactory(Object aBean, Class<?> aTargetClass) {
        LOG.info("Plain: {}", aTargetClass.getSimpleName());
        ProxyFactory proxyFactory = new ProxyFactory(aBean);
        proxyFactory.copyFrom(proxyConfig);
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(pointcut, new SecuredMethodInterceptor()));
        return proxyFactory.getProxy(beanClassLoader);
    }

}
