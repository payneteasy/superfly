package com.payneteasy.superfly.demo;

import javax.naming.NamingException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo {
	 private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

	    /**
	     * -Dwicket.configuration=development or deployment
	     * -Djetty.port=8085
	     */
	    public static void main(String[] args) throws Exception {
	        new Demo().startServer();
	    }
	    
	    public void startServer() throws NamingException {
	        LOG.info("Configuring jetty web server ...");

	        final Server server = new Server();
	        SocketConnector connector = new SocketConnector();
	        // Set some timeout options to make debugging easier.
	        connector.setMaxIdleTime(1000 * 60 * 60);
	        connector.setSoLingerTime(-1);
	        connector.setPort(Integer.parseInt(System.getProperty("jetty.port", "8086")));
	        
	        server.setConnectors(new Connector[]{connector});

	        WebAppContext demo = new WebAppContext();
	        demo.setServer(server);
	        demo.setContextPath("/superfly-demo");
	        demo.setWar("src/main/webapp");
	        demo.setConfigurationClasses(new String[] {
	                "org.mortbay.jetty.webapp.WebInfConfiguration",
	                "org.mortbay.jetty.plus.webapp.EnvConfiguration",
	                "org.mortbay.jetty.plus.webapp.Configuration",
	                "org.mortbay.jetty.webapp.JettyWebXmlConfiguration"
	        });
	        
	        server.addHandler(demo);

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
