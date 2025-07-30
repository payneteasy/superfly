package com.payneteasy.superfly.web;

import com.payneteasy.startup.parameters.StartupParametersFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.webapp.FragmentConfiguration;
import org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee10.webapp.JndiConfiguration;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SuperflyServer {


    private static final int PORT_DISABLED = -1;

    public static final String WEBAPP_WAR_PATH = "/webapp";
    public static final String NEXT_PROTOCOL = "http/1.1";
    public static final String JETTY_SERVLET_DEFAULT_DIR_ALLOWED_PARAM = "org.eclipse.jetty.servlet.Default.dirAllowed";

    public static final String SERVER_CONFIG_ID = "Server";
    public static final String WAC_CONFIG_ID = "wac";

    public void startServer() throws Exception {

        IStartSuperflyConfig config = StartupParametersFactory.getStartupParameters(IStartSuperflyConfig.class);

        final Server server = new Server();
        setupServer(server, config);

        try {
            server.start();
            server.join();
            log.info("Superfly server has been started on port {} and SSL port {}", config.getJettyPort(), config.getJettyPortSsl());
        } catch (Exception e) {
            log.error("Error during server startup", e);
            System.exit(1);
        }
    }

    private void setupServer(final Server server, IStartSuperflyConfig config) throws Exception {

        addConnectors(server, config);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        WebAppContext webAppContext = createWebAppContext(config);
        mergeConfigurations(server, webAppContext, config.getJettyXmlConfigFilePath());
        contexts.setHandlers(webAppContext);
        server.setHandler(contexts);
        server.setStopAtShutdown(true);
        server.setStopTimeout(config.getJettyStopTimeoutMs());
    }

    private void addConnectors(Server server, IStartSuperflyConfig config) {

        HttpConfiguration httpConfiguration = createHttpConfiguration(config);

        if (config.getJettyPort() > PORT_DISABLED) {
            server.addConnector(createConnector(server, config.getJettyPort(), httpConfiguration));
        }
        if (config.getJettyPortSsl() > PORT_DISABLED) {
            server.addConnector(createSslConnector(server, config, httpConfiguration));
        }
    }

    private ServerConnector createSslConnector(Server server, IStartSuperflyConfig config, HttpConfiguration httpConfiguration) {

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePath(config.getJettySslKeystorePath());
        sslContextFactory.setKeyStorePassword(config.getJettySslKeystorePassword());
        sslContextFactory.setTrustStorePath(config.getJettySslTruststorePath());
        sslContextFactory.setTrustStorePassword(config.getJettySslTruststorePassword());
        sslContextFactory.setNeedClientAuth(config.getSslClientAuthRequired());

        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, NEXT_PROTOCOL),
                new HttpConnectionFactory(httpConfiguration));

        connector.setPort(config.getJettyPortSsl());

        return connector;
    }

    private ServerConnector createConnector(Server server, int port, HttpConfiguration httpConfig) {
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        return connector;
    }

    private HttpConfiguration createHttpConfiguration(IStartSuperflyConfig config) {

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

        return httpConfig;
    }

    private WebAppContext createWebAppContext(IStartSuperflyConfig config) {

        WebAppContext webapp = new WebAppContext();

        webapp.setContextPath(config.getJettyContext());
        webapp.setWar(StartSuperfly.class.getResource(WEBAPP_WAR_PATH).toExternalForm());

        webapp.addConfiguration(new WebInfConfiguration(),
                new JettyWebXmlConfiguration(),
                new FragmentConfiguration(),
                new JndiConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration());

        webapp.setInitParameter(JETTY_SERVLET_DEFAULT_DIR_ALLOWED_PARAM, "false");

        return webapp;
    }

    private void mergeConfigurations(Server server, WebAppContext webAppContext, String xmlConfigPath) throws Exception {

        Map<String, Object> idMap = new HashMap<>();
        idMap.put(SERVER_CONFIG_ID, server);
        idMap.put(WAC_CONFIG_ID, webAppContext);

        XmlConfiguration configuration = new XmlConfiguration(
                ResourceFactory.of(server).newResource(xmlConfigPath),
                idMap,
                loadSystemProperties()
        );

        configuration.configure();
    }

    private Map<String, String> loadSystemProperties() {
        return new HashMap<>(System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().toString()
                )));
    }
}
