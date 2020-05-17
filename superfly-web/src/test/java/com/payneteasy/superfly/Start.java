package com.payneteasy.superfly;

import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.util.Collections;

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

        /**
         * @throws NamingException
         */
        public void startServer() throws NamingException {
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
            ServerConnector connector = new ServerConnector(server,
                    new HttpConnectionFactory(httpConfig));
            // Set some timeout options to make debugging easier.
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
            // FIXME: we've enabled SHA|SHA1 back!
            //sslContextFactory.setExcludeCipherSuites("^.*_(MD5)$");

            ServerConnector secureConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(httpsConfig));
            // Set some timeout options to make debugging easier.
            secureConnector.setIdleTimeout(1000 * 60 * 60);
            secureConnector.setPort(Integer.parseInt(System.getProperty("jetty.port.https", "8446")));
            //secureConnector.setHost("localhost");

            server.setConnectors(new Connector[]{connector, secureConnector});

            WebAppContext superfly = new WebAppContext();
            superfly.setServer(server);
            superfly.setContextPath("/superfly");
            superfly.setWar("src/main/webapp");
            superfly.setConfigurations(new Configuration[]{
                    new WebInfConfiguration(),
                    new WebXmlConfiguration(),
                    new MetaInfConfiguration(),
                    new FragmentConfiguration(),
                    new EnvConfiguration(),
                    new PlusConfiguration(),
                    new JettyWebXmlConfiguration()
            });

            superfly.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

            String overrideWebXml = System.getProperty(PROPERTY_OVERRIDE_WEB_XML, "src/test/resources/jetty/override-web.xml");
            LOG.info("Override web.xml is "+overrideWebXml + "( to override use -D"+PROPERTY_OVERRIDE_WEB_XML+")");
            superfly.setOverrideDescriptors(Collections.singletonList(overrideWebXml));

            ContextHandlerCollection webapps = new ContextHandlerCollection();
            webapps.setHandlers(new Handler[]{superfly});
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
                e.printStackTrace();
                System.exit(100);
            }
        }
}
