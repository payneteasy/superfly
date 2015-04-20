package com.payneteasy.superfly;

import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
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

	        final Server server = new Server();
	        SocketConnector connector = new SocketConnector();
	        // Set some timeout options to make debugging easier.
	        connector.setMaxIdleTime(1000 * 60 * 60);
	        connector.setSoLingerTime(-1);
	        connector.setPort(Integer.parseInt(System.getProperty("jetty.port", "8085")));

	        SslSocketConnector secureConnector = new SslSocketConnector();
	        // Set some timeout options to make debugging easier.
	        secureConnector.setMaxIdleTime(1000 * 60 * 60);
	        secureConnector.setSoLingerTime(-1);
	        secureConnector.setPort(Integer.parseInt(System.getProperty("jetty.port.https", "8446")));
			final SslContextFactory sslContextFactory = secureConnector.getSslContextFactory();
			sslContextFactory.setKeyStorePath("src/test/resources/superfly_server_ks");
	        sslContextFactory.setKeyStorePassword("changeit");
	        sslContextFactory.setTrustStore("src/test/resources/ca_ts");
	        sslContextFactory.setTrustStorePassword("changeit");
	        sslContextFactory.setNeedClientAuth(true);
	        
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
	                Thread.sleep(5000);
	            }
	            server.stop();
	            server.join();
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.exit(100);
	        }
	    }
}
