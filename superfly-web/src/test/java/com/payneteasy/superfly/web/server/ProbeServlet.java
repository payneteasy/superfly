package com.payneteasy.superfly.web.server;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;

/** Plain servlet (no Spring stereotype: the production @ComponentScan must not pick up test classes) */
public class ProbeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BasicDataSource dataSource = (BasicDataSource) new InitialContext().lookup("java:comp/env/jdbc/superfly");
            resp.setContentType("text/plain");
            resp.getWriter().print("scheme=" + req.getScheme() + "\nds-url=" + dataSource.getUrl()
                    + "\nds-user=" + dataSource.getUserName());
        } catch (NamingException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
        }
    }
}
