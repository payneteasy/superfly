package com.payneteasy.superfly.web.servlet;

import com.payneteasy.superfly.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author rpuch
 */
public class VersionNumberServlet extends AutowiringServlet {
    @Autowired
    private SettingsService settingsService;

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
