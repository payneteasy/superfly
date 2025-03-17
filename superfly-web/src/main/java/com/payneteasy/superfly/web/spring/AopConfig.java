package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.aop.ExceptionConversionAspect;
import com.payneteasy.superfly.api.SSOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy // Включаем волшебство аспектов
public class AopConfig {

    @Bean
    public ExceptionConversionAspect exceptionConversionAspect() {
        ExceptionConversionAspect    aspect  = new ExceptionConversionAspect();
        Class<? extends Exception>[] classes = new Class[]{SSOException.class};
        aspect.setNonConvertibleClasses(classes);
        return aspect;
    }
}

