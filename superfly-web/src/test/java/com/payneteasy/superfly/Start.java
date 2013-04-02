package com.payneteasy.superfly;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;

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
	        secureConnector.setKeystore("src/test/resources/superfly_server_ks");
	        secureConnector.setKeyPassword("changeit");
	        secureConnector.setTruststore("src/test/resources/ca_ts");
	        secureConnector.setTrustPassword("changeit");
	        secureConnector.setNeedClientAuth(true);
	        
	        server.setConnectors(new Connector[]{connector, secureConnector});

	        WebAppContext superfly = new WebAppContext();
	        superfly.setServer(server);
	        superfly.setContextPath("/superfly");
	        superfly.setWar("src/main/webapp");
	        superfly.setConfigurationClasses(new String[] {
	                "org.mortbay.jetty.webapp.WebInfConfiguration",
	                "org.mortbay.jetty.plus.webapp.EnvConfiguration",
	                "org.mortbay.jetty.plus.webapp.Configuration",
	                "org.mortbay.jetty.webapp.JettyWebXmlConfiguration"
	        });

            String overrideWebXml = System.getProperty(PROPERTY_OVERRIDE_WEB_XML, "src/test/resources/jetty/override-web.xml");
            LOG.info("Override web.xml is "+overrideWebXml + "( to override use -D"+PROPERTY_OVERRIDE_WEB_XML+")");
	        superfly.setOverrideDescriptor(overrideWebXml);
	        
	        server.addHandler(superfly);

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
