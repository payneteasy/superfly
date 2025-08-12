# Руководство по настройке встроенного Jetty сервера

## Содержание
- [Введение](#введение)
- [Системные требования](#системные-требования)
- [Конфигурация сервера](#конфигурация-сервера)
    - [HTTP конфигурация](#http-конфигурация)
    - [Конфигурация HTTP connector](#конфигурация-http-connector)
    - [Конфигурация HTTPS connector](#конфигурация-https-connector)
    - [Конфигурация контекста web-приложения](#конфигурация-контекста-web-приложения)
- [Запуск сервера](#запуск-сервера)

## Введение

Данное руководство описывает процесс настройки и запуска встроенного Jetty сервера для приложения Superfly. Конфигурация выполняется через XML файл, что позволяет легко изменять параметры сервера без перекомпиляции приложения.
Также при желании можно произвести настройку базовых параметров с помощью переменных окружения без использования XML-конфигурации.

## Системные требования

- Java 21 или выше
- Доступ к файловой системе для хранения конфигурационных файлов
- MySQL сервер для базы данных приложения

## Конфигурация сервера

Основной конфигурационный файл для встроенного Jetty сервера представляет собой XML документ, соответствующий спецификации Jetty Configuration.

### HTTP конфигурация

#### Пример параметров с описанием

| Параметр | Пример значения | Описание |
|----------|-----------------|----------|
| `secureScheme` | https           | Определяет схему для безопасных соединений |
| `securePort` | 8013            | Порт для безопасных HTTPS соединений |
| `outputBufferSize` | 32768           | Размер буфера вывода в байтах |
| `requestHeaderSize` | 8192            | Максимальный размер заголовков запроса в байтах |
| `responseHeaderSize` | 8192            | Максимальный размер заголовков ответа в байтах |
| `sendServerVersion` | false           | Отключает отправку версии сервера в HTTP заголовках |
| `sendDateHeader` | false           | Отключает автоматическую отправку заголовка даты |

#### Пример конфигурации:

```xml
<!-- Конфигурация HTTP -->
<New id="httpConfig" class="org.eclipse.jetty.server.HttpConfiguration">
    <Set name="secureScheme">https</Set>
    <Set name="securePort">8013</Set>
    <Set name="outputBufferSize">32768</Set>
    <Set name="requestHeaderSize">8192</Set>
    <Set name="responseHeaderSize">8192</Set>
    <Set name="sendServerVersion">false</Set>
    <Set name="sendDateHeader">false</Set>
</New>
```

### Конфигурация HTTP connector

В этом разделе описана конфигурация HTTP коннектора для Jetty сервера, который обеспечивает обработку входящих HTTP-соединений.

#### Пример параметров с описанием


| Параметр | Пример значения | Описание |
|----------|-----------------|----------|
| `port` | 8010            | Порт, на котором сервер будет принимать HTTP соединения |
| `idleTimeout` | 30000           | Таймаут неактивности в миллисекундах (30 секунд) |

#### Структура коннектора

- **ServerConnector** - основной класс, обрабатывающий входящие соединения
- **HttpConnectionFactory** - фабрика, создающая HTTP соединения
- Коннектор использует HTTP конфигурацию (`httpConfig`), определяющую параметры HTTP протокола


#### Пример из конфигурации:

```xml
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
            <Set name="port">8010</Set>
            <Set name="idleTimeout">30000</Set>
        </New>
    </Arg>
</Call>
```

### Конфигурация HTTPS connector

В этом разделе описана конфигурация HTTPS коннектора для Jetty сервера, который обеспечивает обработку входящих HTTPS-соединений.

#### Описание параметров

#### Основные параметры
| Параметр | Пример значения | Описание |
|----------|-----------------|----------|
| `port` | 8013            | Порт для HTTPS соединений |
| `needClientAuth` | true            | Требуется аутентификация клиента (двусторонний SSL) |

#### Хранилища сертификатов
| Хранилище | Путь                  | Назначение |
|-----------|-----------------------|------------|
| Keystore | `/path/to/server.jks` | Содержит сертификат и закрытый ключ сервера |
| Truststore | `/path/to/cacert.jks` | Содержит доверенные сертификаты для проверки клиентов |

#### Пример из конфигурации:

```xml
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
                                    <Set name="keyStorePath">/path/to/server.jks</Set>
                                    <Set name="keyStorePassword">password</Set>
                                    <Set name="trustStorePath">/path/to/cacert.jks</Set>
                                    <Set name="trustStorePassword">password</Set>
                                    <Set name="needClientAuth">true</Set>
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
            <Set name="port">8013</Set>
        </New>
    </Arg>
</Call>
```

### Конфигурация контекста web-приложения

#### Пример конфигурации:

```xml
<New id="wac" class="org.eclipse.jetty.ee10.webapp.WebAppContext">
    
        <!-- Добавление context params -->
        <Call name="setInitParameter">
            <Arg>superfly-policy</Arg>
            <Arg>pcidss</Arg>
        </Call>
        <Call name="setInitParameter">
            <Arg>configuration</Arg>
            <Arg>deployment</Arg>
        </Call>

        <!-- Конфигурация DataSource -->
        <New id="uiDataSource" class="org.eclipse.jetty.plus.jndi.Resource">
            <Arg>jdbc/superfly</Arg>
            <Arg>
                <New class="org.apache.commons.dbcp2.BasicDataSource">
                    <Set name="driverClassName">com.mysql.cj.jdbc.Driver</Set>
                    <Set name="url">jdbc:mysql://localhost:3306/sso?autoReconnect=false&amp;characterEncoding=utf8&amp;serverTimezone=UTC</Set>
                    <Set name="username">username</Set>
                    <Set name="password">password</Set>
                    <Set name="maxTotal">10</Set>
                    <Set name="maxIdle">5</Set>
                    <Set name="minIdle">2</Set>
                    <Set name="maxWaitMillis">10000</Set>
                    <Set name="testOnBorrow">true</Set>
                    <Set name="validationQuery">{call create_collections()}</Set>
                </New>
            </Arg>
        </New>

    </New>

    <Set name="handler">
        <New class="org.eclipse.jetty.server.handler.ContextHandlerCollection">
            <Call name="addHandler">
                <Arg><Ref refid="wac"/></Arg>
            </Call>
        </New>
    </Set>
```

#### Основные параметры WebAppContext

Данная конфигурация определяет контекст веб-приложения в Jetty с следующими ключевыми настройками:

- **Политика безопасности**: `superfly-policy=pcidss` (соответствует требованиям стандарта PCI DSS)
- **Режим работы**: `configuration=deployment` (производственный режим)

#### База данных

Приложение использует MySQL в качестве СУБД со следующими параметрами:

- **JNDI-имя**: `jdbc/superfly`
- **URL подключения**: `jdbc:mysql://localhost:3306/sso` с UTF-8 кодировкой и часовым поясом UTC
- **Драйвер**: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver)

#### Пул соединений

Для управления соединениями с базой данных используется Apache DBCP2 со следующей конфигурацией:

| Параметр | Значение | Описание |
|----------|----------|----------|
| maxTotal | 10 | Максимум активных соединений |
| maxIdle | 5 | Максимум простаивающих соединений |
| minIdle | 2 | Минимум простаивающих соединений |
| maxWaitMillis | 10000 | Максимальное время ожидания соединения (10 сек) |
| testOnBorrow | true | Проверка соединения перед использованием |

#### Обработчик запросов

Созданный `WebAppContext` добавляется в коллекцию обработчиков контекста (ContextHandlerCollection), что позволяет серверу правильно маршрутизировать HTTP-запросы к приложению.

### Полный пример простой XML-конфигурации сервера

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "https://www.eclipse.org/jetty/configure_12_0.dtd">
<Configure id="Server" class="org.eclipse.jetty.server.Server">
    <!-- Конфигурация HTTP -->
    <New id="httpConfig" class="org.eclipse.jetty.server.HttpConfiguration">
        <Set name="secureScheme">https</Set>
        <Set name="securePort">8013</Set>
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
                <Set name="port">8010</Set>
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
                                        <Set name="keyStorePath">/path/to/server.jks</Set>
                                        <Set name="keyStorePassword">password</Set>
                                        <Set name="keyManagerPassword">password</Set>
                                        <Set name="trustStorePath">/path/to/cacert.jks</Set>
                                        <Set name="trustStorePassword">password</Set>
                                        <Set name="needClientAuth">true</Set>
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
                <Set name="port">8013</Set>
            </New>
        </Arg>
    </Call>

    <New id="wac" class="org.eclipse.jetty.ee10.webapp.WebAppContext">
      
        <!-- Добавление context params -->
        <Call name="setInitParameter">
            <Arg>superfly-policy</Arg>
            <Arg>pcidss</Arg>
        </Call>
        <Call name="setInitParameter">
            <Arg>logbackConfigLocation</Arg>
            <Arg>/path/to/logback.xml</Arg>
        </Call>
        <Call name="setInitParameter">
            <Arg>configuration</Arg>
            <Arg>deployment</Arg>
        </Call>

        <!-- Конфигурация DataSource -->
        <New id="uiDataSource" class="org.eclipse.jetty.plus.jndi.Resource">
            <Arg>jdbc/superfly</Arg>
            <Arg>
                <New class="org.apache.commons.dbcp2.BasicDataSource">
                    <Set name="driverClassName">com.mysql.cj.jdbc.Driver</Set>
                    <Set name="url">jdbc:mysql://localhost:3306/sso?autoReconnect=false&amp;characterEncoding=utf8&amp;serverTimezone=UTC</Set>
                    <Set name="username">username</Set>
                    <Set name="password">password</Set>
                    <Set name="maxTotal">10</Set>
                    <Set name="maxIdle">5</Set>
                    <Set name="minIdle">2</Set>
                    <Set name="maxWaitMillis">10000</Set>
                    <Set name="testOnBorrow">true</Set>
                    <Set name="validationQuery">{call create_collections()}</Set>
                </New>
            </Arg>
        </New>

    </New>

    <Set name="handler">
        <New class="org.eclipse.jetty.server.handler.ContextHandlerCollection">
            <Call name="addHandler">
                <Arg><Ref refid="wac"/></Arg>
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

### Запуск сервера

#### Пример запуска сервера:

```shell
export JETTY_XML_CONFIG_FILE_PATH=/home/sso/opt/sso-ejetty/conf/server2.xml
export JETTY_CONTEXT=/sso

java -Dlogback.configurationFile=logback.xml -jar superfly.jar 
```
#### Описание альтернативной настройки сервера через переменные окружения:

#### Список доступных параметров

| Параметр | Описание | Значение по умолчанию |
|----------|----------|----------------------|
| `JETTY_PORT` | Порт Jetty для HTTP соединений | `-1` (отключено) |
| `JETTY_PORT_SSL` | Порт Jetty для HTTPS соединений | `-1` (отключено) |
| `JETTY_SSL_TRUSTSTORE_PATH` | Путь к truststore для SSL | `config/truststore.jks` |
| `JETTY_SSL_TRUSTSTORE_PASSWORD` | Пароль для truststore | `changeit` |
| `JETTY_SSL_KEYSTORE_PATH` | Путь к keystore для SSL | `config/keystore.jks` |
| `JETTY_SSL_KEYSTORE_PASSWORD` | Пароль для keystore | `changeit` |
| `JETTY_MAX_THREADS` | Максимальное количество потоков в пуле Jetty | `500` |
| `JETTY_MIN_THREADS` | Минимальное количество потоков в пуле Jetty | `16` |
| `JETTY_CONTEXT` | Корневой контекст приложения | `/` |
| `JETTY_OUTPUT_BUFFER_SIZE` | Размер выходного буфера в байтах | `32768` |
| `JETTY_HEADER_SIZE` | Максимальный размер HTTP заголовка в байтах | `8192` |
| `JETTY_SEND_SERVER_VERSION` | Отправлять версию сервера в HTTP заголовках | `true` |
| `JETTY_SEND_DATE_HEADER` | Отправлять дату в HTTP заголовках | `true` |
| `JETTY_SECURE_SCHEME` | Схема для безопасного соединения | `https` |
| `JETTY_XML_CONFIG_FILE_PATH` | Путь к XML файлу конфигурации Jetty | `/path/to/jetty-test-config.xml` |
| `JETTY_SSL_CLIENT_AUTH_REQUIRED` | Требовать аутентификацию клиента по SSL | `true` |
| `JETTY_STOP_TIMEOUT_MS` | Таймаут для корректного завершения работы Jetty (в миллисекундах) | `5000` |

#### Пример настройки через shell-скрипт

Ниже приведен пример shell-скрипта для установки параметров конфигурации:

```shell
#!/bin/bash

# Настройки Jetty
export JETTY_PORT=8080
export JETTY_PORT_SSL=8443
export JETTY_SSL_TRUSTSTORE_PATH=/opt/superfly/config/truststore.jks
export JETTY_SSL_TRUSTSTORE_PASSWORD=my-secure-password
export JETTY_SSL_KEYSTORE_PATH=/opt/superfly/config/keystore.jks
export JETTY_SSL_KEYSTORE_PASSWORD=my-secure-password
export JETTY_MAX_THREADS=200
export JETTY_MIN_THREADS=20
export JETTY_CONTEXT=/superfly
export JETTY_OUTPUT_BUFFER_SIZE=65536
export JETTY_HEADER_SIZE=16384
export JETTY_SEND_SERVER_VERSION=false
export JETTY_SEND_DATE_HEADER=true
export JETTY_SECURE_SCHEME=https
export JETTY_XML_CONFIG_FILE_PATH=/opt/superfly/config/jetty-config.xml
export JETTY_SSL_CLIENT_AUTH_REQUIRED=false
export JETTY_STOP_TIMEOUT_MS=10000
```
