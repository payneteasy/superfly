[← Установка и запуск](getting-started.md) · [Back to README](../README.md) · [Руководство по интеграции →](integration-guide.md)

# Конфигурация

## База данных

Конфигурация DataSource задаётся в Jetty XML-файлах.

### `superfly-web/src/main/webapp/WEB-INF/jetty-web.xml`

Основной конфиг для деплоя на Jetty. Используется в production.

```xml
<Configure id='wac' class="org.eclipse.jetty.ee10.webapp.WebAppContext">
    <New id="txDatasource" class="org.eclipse.jetty.plus.jndi.Resource">
        <Arg>java:comp/env/jdbc/superfly</Arg>
        <Arg>
            <New class="org.apache.commons.dbcp2.BasicDataSource">
                <Set name="url">jdbc:mysql://HOST:PORT/sso
                    ?characterEncoding=utf8
                    &amp;useInformationSchema=true
                    &amp;noAccessToProcedureBodies=false
                    &amp;useLocalSessionState=true
                    &amp;autoReconnect=false
                    &amp;serverTimezone=Europe/Moscow</Set>
                <Set name="driverClassName">com.mysql.cj.jdbc.Driver</Set>
                <Set name="username">sso</Set>
                <Set name="password">YOUR_PASSWORD</Set>
                <Set name="testOnBorrow">true</Set>
                <Set name="validationQuery">{call create_collections()}</Set>
            </New>
        </Arg>
    </New>
</Configure>
```

### `superfly-web/src/main/resources/jetty-env.conf`

Конфиг для разработки через `mvn jetty:run`. Структура аналогична `jetty-web.xml`, но использует `org.apache.commons.dbcp.BasicDataSource` (dbcp1).

| Параметр | Описание |
|----------|---------|
| `url` | JDBC URL. Порт по умолчанию: `3306`. Схема: `sso` |
| `username` | Пользователь MySQL |
| `password` | Пароль MySQL |
| `serverTimezone` | Часовой пояс MySQL-сервера (например, `UTC`, `Europe/Moscow`) |
| `testOnBorrow` | Проверять соединение при взятии из пула |
| `validationQuery` | SQL-вызов для проверки соединения |

## Параметры запуска Jetty

Порт и другие параметры Jetty задаются при запуске через Maven:

```bash
# Изменить порт (по умолчанию 8080)
./mvnw -pl superfly-web jetty:run -Djetty.http.port=9090
```

Или в `superfly-web/pom.xml` в конфигурации `jetty-maven-plugin`.

## Spring Application Context

Конфигурационные XML-файлы Spring находятся в `superfly-web/src/main/webapp/WEB-INF/`:

| Файл | Назначение |
|------|-----------|
| `applicationContext.xml` | Основной контекст приложения |
| `security.xml` | Spring Security конфигурация |
| `mvc-dispatcher-servlet.xml` | Spring MVC диспетчер |

## Политика паролей

Superfly поддерживает несколько политик паролей. Активная политика задаётся через Spring-конфигурацию с условием `@OnPolicyCondition`:

| Значение | Описание |
|----------|---------|
| `NONE` | Без политики (пароли не хэшируются) |
| `PCIDSS` | Политика PCI DSS: минимум 8 символов, спецсимволы |

## SMTP (Email-уведомления)

SMTP-серверы настраиваются через веб-интерфейс администратора (`/smtpServer/list`), а не через файлы конфигурации.

## HOTP / Двухфакторная аутентификация

HOTP-провайдер настраивается через `superfly-spi`. Реализация подключается через Spring DI. По умолчанию используется `NullHOTPProvider` (2FA отключена).

## Переменные окружения

На данный момент все параметры задаются через XML-конфиги Jetty и Spring, а не через переменные окружения. Для контейнеризации рекомендуется использовать внешние конфиги с volume-монтированием `jetty-web.xml`.

## See Also

- [Установка и запуск](getting-started.md) — начальная настройка БД
- [Руководство по интеграции](integration-guide.md) — настройка клиентских приложений
