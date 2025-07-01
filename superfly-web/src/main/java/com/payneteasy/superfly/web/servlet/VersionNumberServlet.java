package com.payneteasy.superfly.web.servlet;

import com.payneteasy.superfly.service.SettingsService;
import jakarta.servlet.ServletConfig;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author rpuch
 */
public class VersionNumberServlet extends AutowiringServlet {
    private SettingsService settingsService;

    @Override
    protected void doInit(ServletConfig config) throws ServletException {
        super.doInit(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String version = settingsService.getSuperflyVersion();
        String path = request.getRequestURI();

        final PrintWriter out = response.getWriter();
        out.print(version);
        out.flush();
        out.close();
    }
}
