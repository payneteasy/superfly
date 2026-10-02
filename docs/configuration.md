[← Установка и запуск](getting-started.md) · [Back to README](../README.md) · [Доменная модель →](domain-model.md)

# Конфигурация

## База данных

Конфигурация DataSource задаётся в Jetty XML-файлах.

### Production (Docker): `docker/jetty/ROOT.xml`

В образе DataSource описан в `docker/jetty/ROOT.xml` и читает параметры из **переменных окружения**:
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_TIMEZONE` (порт Jetty — `JETTY_PORT`).
Пароль передавайте только через окружение/секреты. Запуск — в [Установке и запуске](getting-started.md#docker-образ).

### Ключ шифрования OTP master key

Master key TOTP хранится в БД зашифрованным (AES-256-GCM, ключ выводится из секрета и соли через PBKDF2).
Обязательные переменные окружения (дефолтов нет — без них приложение не стартует; значения-заглушки
`GOOGLE_AUTH_OTP_*` тоже отвергаются):

| Переменная | Назначение |
|------------|-----------|
| `SUPERFLY_CRYPTO_SECRET` | секрет для вывода ключа |
| `SUPERFLY_CRYPTO_SALT` | соль для вывода ключа |

Как запасной вариант читаются одноимённые по смыслу системные свойства / context-параметры
`superfly-cryptoSecret` и `superfly-cryptoSalt`. **Менять значения нельзя без перешифровки:** уже сохранённые
ключи перестанут расшифровываться. Старые ключи (AES-CBC) читаются как раньше; новые пишутся только в формате `v2:`.

### Dev: `superfly-web/src/main/webapp/WEB-INF/jetty-web.xml`

Конфиг для локального запуска (`./dev-env.sh app`, `Start.java`) с dev-базой на `localhost:3344`.
Исключён из WAR (`packagingExcludes`), поэтому в контейнере и на сервере не используется. Структура:

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

Вариант DataSource с параметрами из system properties (`db.host`, `db.port`, `db.name`, `db.user`, `db.password`, `db.timezone`) на `org.apache.commons.dbcp.BasicDataSource` (dbcp1).

| Параметр | Описание |
|----------|---------|
| `url` | JDBC URL. Порт по умолчанию: `3306`. Схема: `sso` |
| `username` | Пользователь MySQL |
| `password` | Пароль MySQL |
| `serverTimezone` | Часовой пояс MySQL-сервера (например, `UTC`, `Europe/Moscow`) |
| `testOnBorrow` | Проверять соединение при взятии из пула |
| `validationQuery` | SQL-вызов для проверки соединения |

## Параметры запуска Jetty

- **Docker:** порт задаёт `JETTY_PORT` (по умолчанию 8080), см. `docker/jetty/entrypoint.sh`.
- **Локально:** `./dev-env.sh app` запускает `Start.java` на `http://localhost:8085/superfly/`.

Стадии `development` и `jetty:run` в Docker/Maven больше нет.

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
| `NONE` | Без политики сложности. Пароли хэшируются SHA-256 с солью из `users.salt`, как и при `PCIDSS` |
| `PCIDSS` | Политика PCI DSS: минимум 8 символов, спецсимволы |

## SMTP (Email-уведомления)

SMTP-серверы настраиваются через веб-интерфейс администратора (`/smtpServer/list`), а не через файлы конфигурации.

## HOTP / Двухфакторная аутентификация

HOTP-провайдер настраивается через `superfly-spi`. Реализация подключается через Spring DI. По умолчанию используется `NullHOTPProvider` (2FA отключена).

## Content-Security-Policy

Origin из `landingUrl` и `subsystemUrl` всех подсистем добавляются в директиву `form-action`, а `loginFormCssUrl` — в `style-src`.
Список кэшируется на 5 минут и сбрасывается сразу при правке подсистемы в админке. После сбоя загрузки (БД недоступна) в течение 10 секунд отдаётся базовая политика без обращений к БД.

## Переменные окружения

Подключение к БД в Docker-образе задаётся `DB_*` и `JETTY_PORT` (см. выше). Остальные параметры — через XML-конфиги Jetty и Spring.

## See Also

- [Установка и запуск](getting-started.md) — начальная настройка БД
- [Руководство по интеграции](integration-guide.md) — настройка клиентских приложений
