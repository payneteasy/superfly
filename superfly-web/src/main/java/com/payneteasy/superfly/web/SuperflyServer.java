package com.payneteasy.superfly.web;

import com.payneteasy.startup.parameters.StartupParametersFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.ee10.webapp.Configuration;
import org.eclipse.jetty.ee10.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.ee10.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.ee10.webapp.FragmentConfiguration;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

/**
 * Embedded Jetty serving the web application packed into the same JAR (classpath resource /webapp).
 * The JNDI datasource java:comp/env/jdbc/superfly is created from the DB_* settings.
 */
public class SuperflyServer {

    private static final Logger LOG = LoggerFactory.getLogger(SuperflyServer.class);

    private static final int PORT_DISABLED = -1;

    public static final String WEBAPP_RESOURCE = "/webapp";
    public static final String NEXT_PROTOCOL = "http/1.1";
    public static final String JNDI_DATASOURCE_NAME = "jdbc/superfly";
    public static final String SERVER_CONFIG_ID = "Server";
    public static final String WAC_CONFIG_ID = "wac";

    private final URL webappUrl;

    public SuperflyServer() {
        this(SuperflyServer.class.getResource(WEBAPP_RESOURCE));
    }

    /** @param webappUrl root of the web application (the directory containing WEB-INF) */
    public SuperflyServer(URL webappUrl) {
        this.webappUrl = webappUrl;
    }

    public void startServer() throws Exception {
        IStartSuperflyConfig config = StartupParametersFactory.getStartupParameters(IStartSuperflyConfig.class);
        Server server = createServer(config);
        try {
            server.start();
        } catch (Exception e) {
            LOG.error("Error during server startup", e);
            server.stop();
            System.exit(1);
        }
        server.join();
    }

    public Server createServer(IStartSuperflyConfig config) throws Exception {
        Server server = new Server(new QueuedThreadPool(config.getJettyMaxThreads(), config.getJettyMinThreads()));
        addConnectors(server, config);

        WebAppContext webAppContext = createWebAppContext(config);
        server.setHandler(webAppContext);
        server.setStopAtShutdown(true);
        server.setStopTimeout(config.getJettyStopTimeoutMs());

        String xmlConfigPath = config.getJettyXmlConfigFilePath();
        if (xmlConfigPath != null && !xmlConfigPath.isBlank()) {
            applyXmlConfiguration(server, webAppContext, xmlConfigPath);
        }
        return server;
    }

    private void addConnectors(Server server, IStartSuperflyConfig config) {
        HttpConfiguration httpConfiguration = createHttpConfiguration(config);

        if (config.getJettyPort() > PORT_DISABLED) {
            ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
            connector.setPort(config.getJettyPort());
            server.addConnector(connector);
        }
        if (config.getJettyPortSsl() > PORT_DISABLED) {
            server.addConnector(createSslConnector(server, config, httpConfiguration));
        }
    }

    private ServerConnector createSslConnector(Server server, IStartSuperflyConfig config, HttpConfiguration httpConfiguration) {
        if (isBlank(config.getJettySslKeystorePath())) {
            throw new IllegalStateException("JETTY_PORT_SSL is set, but JETTY_SSL_KEYSTORE_PATH is empty");
        }
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(config.getJettySslKeystorePath());
        sslContextFactory.setKeyStorePassword(config.getJettySslKeystorePassword());
        if (!isBlank(config.getJettySslTruststorePath())) {
            sslContextFactory.setTrustStorePath(config.getJettySslTruststorePath());
            sslContextFactory.setTrustStorePassword(config.getJettySslTruststorePassword());
        } else if (config.getSslClientAuthRequired()) {
            throw new IllegalStateException("JETTY_SSL_CLIENT_AUTH_REQUIRED is set, but JETTY_SSL_TRUSTSTORE_PATH is empty");
        }
        sslContextFactory.setNeedClientAuth(config.getSslClientAuthRequired());

        HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, NEXT_PROTOCOL),
                new HttpConnectionFactory(httpsConfiguration));
        connector.setPort(config.getJettyPortSsl());
        return connector;
    }

    public HttpConfiguration createHttpConfiguration(IStartSuperflyConfig config) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme(config.getSecureScheme());
        if (config.getJettyPortSsl() > PORT_DISABLED) {
            httpConfig.setSecurePort(config.getJettyPortSsl());
        }
        httpConfig.setOutputBufferSize(config.getOutputBufferSize());
        httpConfig.setRequestHeaderSize(config.getHeaderSize());
        httpConfig.setResponseHeaderSize(config.getHeaderSize());
        httpConfig.setSendServerVersion(config.isSendServerVersion());
        httpConfig.setSendDateHeader(config.isSendDateHeader());
        if (config.isTrustForwarded()) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }
        return httpConfig;
    }

    private WebAppContext createWebAppContext(IStartSuperflyConfig config) throws Exception {
        if (webappUrl == null) {
            throw new IllegalStateException("Web application resources " + WEBAPP_RESOURCE + " not found on the classpath");
        }

        WebAppContext context = new WebAppContext();
        context.setContextPath(config.getJettyContext());
        // a webapp that failed to start (no DB, bad settings) must stop the process, not leave a Jetty answering 503
        context.setThrowUnavailableOnStartupException(true);
        context.setBaseResource(ResourceFactory.of(context).newResource(webappUrl));
        // webdefault.xml names Jetty servlet classes (DefaultServlet, IntrospectorCleaner), but Jetty classes are
        // hidden from the webapp class loader by default; here server and webapp share one classpath
        context.getHiddenClassMatcher().add("-org.eclipse.jetty.ee10.servlet.");
        context.setConfigurations(new Configuration[]{
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration(),
                new PlusConfiguration()});
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        // binds the datasource to java:comp/env of this webapp; web.xml declares the matching resource-ref
        new Resource(context, JNDI_DATASOURCE_NAME, createDataSource(config));
        return context;
    }

    BasicDataSource createDataSource(IStartSuperflyConfig config) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName()
                + "?characterEncoding=utf8&useInformationSchema=true&noAccessToProcedureBodies=false"
                + "&useLocalSessionState=true&autoReconnect=false&serverTimezone=" + config.getDbTimezone());
        dataSource.setUsername(config.getDbUser());
        dataSource.setPassword(config.getDbPassword());
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("{call create_collections()}");
        return dataSource;
    }

    private void applyXmlConfiguration(Server server, WebAppContext webAppContext, String xmlConfigPath) throws Exception {
        XmlConfiguration configuration = new XmlConfiguration(
                ResourceFactory.of(server).newResource(xmlConfigPath),
                Map.of(SERVER_CONFIG_ID, server, WAC_CONFIG_ID, webAppContext),
                Map.of());
        configuration.configure();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
