Пример конфигурации сервера через xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "https://www.eclipse.org/jetty/configure_12_0.dtd">

<!-- Пример конфигурации сервера и веб приложения -->

<Configure id="Server" class="org.eclipse.jetty.server.Server">
    <!-- Конфигурация HTTP -->
    <New id="httpConfig" class="org.eclipse.jetty.server.HttpConfiguration">
        <Set name="secureScheme">https</Set>
        <Set name="securePort">8443</Set>
        <Set name="outputBufferSize">32768</Set>
        <Set name="requestHeaderSize">8192</Set>
        <Set name="responseHeaderSize">8192</Set>
        <Set name="sendServerVersion">false</Set>
        <Set name="sendDateHeader">false</Set>
    </New>

    <!-- HTTP Connector -->
    <Call name="addConnector">
        <Arg>
            <New id="httpConnector" class="org.eclipse.jetty.server.ServerConnector">
                <Arg name="server"><Ref refid="Server"/></Arg>
                <Arg name="factories">
                    <Array type="org.eclipse.jetty.server.ConnectionFactory">
                        <Item>
                            <New class="org.eclipse.jetty.server.HttpConnectionFactory">
                                <Arg><Ref refid="httpConfig"/></Arg>
                            </New>
                        </Item>
                    </Array>
                </Arg>
                <Set name="port">8080</Set>
                <Set name="idleTimeout">30000</Set>
            </New>
        </Arg>
    </Call>

    <!-- HTTPS конфигурация -->
    <New id="sslHttpConfig" class="org.eclipse.jetty.server.HttpConfiguration">
        <Arg><Ref refid="httpConfig"/></Arg>
        <Call name="addCustomizer">
            <Arg>
                <New class="org.eclipse.jetty.server.SecureRequestCustomizer">
                    <Arg type="boolean">false</Arg>
                </New>
            </Arg>
        </Call>
    </New>

    <!-- SSL Connector -->
    <Call name="addConnector">
        <Arg>
            <New id="sslConnector" class="org.eclipse.jetty.server.ServerConnector">
                <Arg name="server"><Ref refid="Server"/></Arg>
                <Arg name="factories">
                    <Array type="org.eclipse.jetty.server.ConnectionFactory">
                        <Item>
                            <New class="org.eclipse.jetty.server.SslConnectionFactory">
                                <Arg name="next">http/1.1</Arg>
                                <Arg>
                                    <New class="org.eclipse.jetty.util.ssl.SslContextFactory$Server">
                                        <Set name="keyStorePath">/path/to/keystore.jks</Set>
                                        <Set name="keyStorePassword">keystorepass</Set>
                                        <Set name="keyManagerPassword">keypassword</Set>
                                        <Set name="trustStorePath">/path/to/truststore.jks</Set>
                                        <Set name="trustStorePassword">truststorepass</Set>
                                    </New>
                                </Arg>
                            </New>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.server.HttpConnectionFactory">
                                <Arg><Ref refid="sslHttpConfig"/></Arg>
                            </New>
                        </Item>
                    </Array>
                </Arg>
                <Set name="port">8443</Set>
            </New>
        </Arg>
    </Call>

    <!-- Конфигурация DataSource -->
    <New id="myDS" class="org.eclipse.jetty.plus.jndi.Resource">
        <Arg>jdbc/myDataSource</Arg>
        <Arg>
            <New class="org.apache.commons.dbcp2.BasicDataSource">
                <Set name="driverClassName">com.mysql.cj.jdbc.Driver</Set>
                <Set name="url">jdbc:mysql://localhost:3306/mydb?serverTimezone=UTC</Set>
                <Set name="username">dbuser</Set>
                <Set name="password">dbpass</Set>
                <Set name="maxTotal">10</Set>
                <Set name="maxIdle">5</Set>
                <Set name="minIdle">2</Set>
                <Set name="maxWaitMillis">10000</Set>
                <Set name="testOnBorrow">true</Set>
                <Set name="validationQuery">SELECT 1</Set>
            </New>
        </Arg>
    </New>

    <!-- Конфигурация WebApp -->
    <Set name="handler">
        <New class="org.eclipse.jetty.ee10.webapp.WebAppContext">
            <Set name="contextPath">/myapp</Set>
            <Set name="war">/path/to/myapp.war</Set>
            <Set name="extractWar">true</Set>
            <Set name="configurationDiscovered">true</Set>

            <!-- Конфигурации WebApp -->
            <Call name="setConfigurations">
                <Arg>
                    <Array type="org.eclipse.jetty.webapp.Configuration">
                        <Item>
                            <New class="org.eclipse.jetty.ee10.webapp.WebInfConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.ee10.webapp.WebXmlConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.ee10.webapp.MetaInfConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.ee10.webapp.FragmentConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.plus.webapp.EnvConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.plus.webapp.PlusConfiguration"/>
                        </Item>
                        <Item>
                            <New class="org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration"/>
                        </Item>
                    </Array>
                </Arg>
            </Call>

            <!-- Инициализационные параметры -->
            <Call name="setInitParameter">
                <Arg>org.eclipse.jetty.servlet.Default.dirAllowed</Arg>
                <Arg>false</Arg>
            </Call>
        </New>
    </Set>

    <!-- Настройки сервера -->
    <Set name="stopAtShutdown">true</Set>
    <Set name="stopTimeout">5000</Set>
    <Set name="dumpAfterStart">false</Set>
    <Set name="dumpBeforeStop">false</Set>
</Configure>
```