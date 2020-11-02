package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.deflt.DefaultResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class ResetPasswordStrategyFactoryBean implements FactoryBean<ResetPasswordStrategy> {
    private ResetPasswordStrategy resetPasswordStrategy;
    private UserService userService;
    private UserPasswordEncoder userPasswordEncoder;
    private LoggerSink loggerSink;

    @Required
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Required
    public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder) {
        this.userPasswordEncoder = userPasswordEncoder;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ResetPasswordStrategy getObject() throws Exception {
        if (resetPasswordStrategy == null) {
            resetPasswordStrategy = new DefaultResetPasswordStrategy(userService,
                    userPasswordEncoder, loggerSink);
        }
        return resetPasswordStrategy;
    }

    public Class<?> getObjectType() {
        return ResetPasswordStrategy.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
