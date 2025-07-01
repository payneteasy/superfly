package com.payneteasy.superfly.web.servlet;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

/**
 * @author rpuch
 */
public abstract class AutowiringServlet extends HttpServlet {
    @Override
    public final void init(ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        doInit(config);
    }

    protected void doInit(ServletConfig config) throws ServletException {
    }
}
