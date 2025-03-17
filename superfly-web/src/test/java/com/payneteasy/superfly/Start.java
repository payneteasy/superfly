package com.payneteasy.superfly;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.ee10.webapp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Slf4j
public class Start {
    private static final Logger LOG = LoggerFactory.getLogger(Start.class);
    private static final String PROPERTY_OVERRIDE_WEB_XML = "override-web-xml";

    /**
     * -Dwicket.configuration=development or deployment
     * -Djetty.port=8085
     */
    public static void main(String[] args) throws Exception {
        new Start().startServer();
    }

    public void startServer()  {
        LOG.info("Configuring jetty web server ...");

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(8443);
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setRequestHeaderSize(8192);
        httpConfig.setResponseHeaderSize(8192);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);

        final Server server = new Server();
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setIdleTimeout(1000 * 60 * 60);
        connector.setPort(Integer.parseInt(System.getProperty("jetty.port", "8085")));

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("src/test/resources/superfly-server.jks");
        sslContextFactory.setKeyStorePassword("changeit");
        sslContextFactory.setTrustStorePath("src/test/resources/cacert.jks");
        sslContextFactory.setTrustStorePassword("changeit");
        sslContextFactory.setNeedClientAuth(true);

        ServerConnector secureConnector = new ServerConnector(server,
                                                              new SslConnectionFactory(sslContextFactory, "http/1.1"),
                                                              new HttpConnectionFactory(httpsConfig));
        secureConnector.setIdleTimeout(1000 * 60 * 60);
        secureConnector.setPort(Integer.parseInt(System.getProperty("jetty.port.https", "8446")));

        server.setConnectors(new Connector[]{connector, secureConnector});

        WebAppContext superfly = new WebAppContext();
        superfly.setContextPath("/superfly");
        superfly.setWar("src/main/webapp");

        // Добавляем конфигурации
        // superfly.mo
        superfly.addConfiguration(new WebInfConfiguration(),
                                  new JettyWebXmlConfiguration(),
                                  new FragmentConfiguration(),
                                  new JndiConfiguration(),
                                  new WebXmlConfiguration(),
                                  new MetaInfConfiguration());

        superfly.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        String overrideWebXml = System.getProperty(PROPERTY_OVERRIDE_WEB_XML, "src/test/resources/jetty/override-web.xml");
        LOG.info("Override web.xml is " + overrideWebXml + " (to override use -D" + PROPERTY_OVERRIDE_WEB_XML + ")");
        superfly.setOverrideDescriptors(Collections.singletonList(overrideWebXml));

        ContextHandlerCollection webapps = new ContextHandlerCollection();
        webapps.setHandlers(superfly);
        server.setHandler(webapps);

        try {
            System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
            server.start();

            while (System.in.available() == 0) {
                Thread.sleep(500);
            }
            server.stop();
            server.join();
        } catch (Exception e) {
            log.error("JETTY SERVER ERROR", e);
            System.exit(100);
        }
    }
}
