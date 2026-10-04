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
Обязательные переменные (дефолтов нет — без них приложение не стартует; значения-заглушки
`GOOGLE_AUTH_OTP_*` тоже отвергаются):

| Переменная | Назначение |
|------------|-----------|
| `SUPERFLY_CRYPTO_SECRET` | секрет для вывода ключа |
| `SUPERFLY_CRYPTO_SALT` | соль для вывода ключа |
| `SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY` | `true` — читать старые шифротексты ключом от прежних дефолтов (по умолчанию `false`) |

Приоритет источников: переменная окружения > системное свойство `-D` > context-param
(`superfly-cryptoSecret`, `superfly-cryptoSalt`, `superfly-cryptoLegacyDefaultKey`).
`compose.production.yml` требует `SECRET` и `SALT` (без них `docker compose` не запустится), в `compose.yml` они
передаются как есть и проверка выполняется самим приложением.

**Менять SECRET/SALT без перешифровки нельзя:** сохранённые ключи перестанут расшифровываться.
Новые значения пишутся только в формате `v2:`; старые (AES-CBC) читаются, а `CryptoService.isLegacy` позволяет их найти.

#### Миграция с дефолтного ключа

Если на инсталляции ключ не задавался (использовались `GOOGLE_AUTH_OTP_SECRET`/`GOOGLE_AUTH_OTP_SALT`):

0. Сначала накатите миграцию `superfly-sql/mi/R1.7.5` (расширяет `users.master_key` до `varchar(128)`: шифротекст `v2:`
   занимает 83 символа) и переустановите процедуры (`superfly-sql/src/all-proc.sh`), только потом выкатывайте образ.
   Без миграции запись нового ключа (в том числе перешифровка и `resetGoogleAuthMasterKey`) падает.
1. Задайте настоящие `SUPERFLY_CRYPTO_SECRET` и `SUPERFLY_CRYPTO_SALT`.
2. Установите `SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY=true`: старые шифротексты читаются прежним дефолтным ключом
   (в лог пишется предупреждение), новые шифруются настоящим.
3. Перешифровка происходит автоматически при следующем успешном вводе TOTP каждым пользователем
   (значение пересохраняется в формате `v2:`). Остаток старых значений показывает запрос:
   `select count(*) from users where master_key is not null and master_key not like 'v2:%'`.
   Когда он вернёт 0, верните `false`.
4. Пользователи, не входившие до выключения флага, потеряют TOTP: им нужно сбросить ключ
   (`resetGoogleAuthMasterKey` / админка).

Пока флаг `true`, старые строки из дампа БД расшифровываются публично известным ключом.

Если на проде ключ был нестандартным, передайте те же значения в `SUPERFLY_CRYPTO_*` и оставьте флаг `false`.

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
| `NONE` | Без политики сложности. Пароли хэшируются PBKDF2 с солью из `users.salt`, как и при `PCIDSS` |
| `PCIDSS` | Политика PCI DSS: минимум 12 символов, спецсимволы |

### Хранение паролей

Пароли хэшируются PBKDF2WithHmacSHA256 (600000 итераций, 256 бит, соль — `users.salt`); формат в `users.user_password`
и `user_history.user_password`: `pbkdf2-sha256$600000$<hex64>`. Старые хэши (SHA-256 от `пароль{соль}`, 64 hex-символа)
остаются рабочими: при успешном входе хэш автоматически перезаписывается в новый формат (в SQL-функции
`int_check_user_password`, только при успехе). Пользователи, не входившие после обновления, остаются в старом формате;
посчитать их можно запросом `select count(*) from users where user_password not like 'pbkdf2-sha256$%'`.
Стоимость — около 0.3–0.5 с CPU на PBKDF2: вход = 1 PBKDF2 + 1 SHA-256 (оба хэша передаются в SQL),
смена пароля = до `historyLength + 1` PBKDF2 на проверку истории (при PCIDSS — порядка 2–2.5 с).
Проверка истории паролей выбирает алгоритм по формату записи.

**Выкатка.** Хранимые процедуры (`all-proc.sh`) и WAR нужно выкатывать одновременно: сигнатуры `authenticate`,
`get_user_login_status` и `int_check_user_password` изменились (добавлен параметр с legacy-хэшем), при рассинхроне
падают все входы.

## Аудит

Журнал событий безопасности (PCI DSS 10.2) пишется в обычный лог приложения (SLF4J) через `LoggerSink`. Таблицы аудита
в БД нет: хранение, защиту от изменения и срок хранения (PCI DSS 10.3, 10.5, 10.7) обеспечивает лог-коллектор —
**stdout контейнера должен отправляться в SIEM / централизованный сборщик логов**, локальных логов недостаточно.

**Формат.** Одна строка на событие, поля `ключ:значение` через запятую:

```
user:admin, event:CHANGE_USER_OTP_OPTIONAL, resource:bob, result:success, details:otpOptional=true, ip:10.1.2.3
```

| Поле | Значение |
|------|----------|
| `user` | Кто действует: логин администратора или имя подсистемы (для remote API); `<SYSTEM>` — нет аутентифицированного субъекта (планировщик, автоблокировка, неудачный вход) |
| `event` | Тип события (таблица ниже) |
| `resource` | Над кем/чем: имя пользователя, id, имя подсистемы или SMTP-сервера |
| `result` | `success` (уровень INFO) или `failure` (уровень ERROR) |
| `details` | Необязательно. Что именно изменено (`role=..., subsystem=...`, `otpOptional=...`); секретов здесь не бывает |
| `ip` | Необязательно. Адрес клиента запроса; нет у событий вне HTTP-запроса. За reverse proxy при `JETTY_TRUST_FORWARDED` — реальный адрес клиента |

Значения очищаются от управляющих символов (CR, LF, TAB, `\u0085`, ` `, ` ` → `_`), чтобы введённый пользователем
текст не мог подделать запись. Ограничение: запятая и `:` не экранируются, парсер SIEM должен брать первое вхождение
каждого ключа.

**События.** Помимо CRUD-операций над пользователями, ролями, группами, подсистемами и SMTP-серверами
(`CREATE_USER`, `UPDATE_ROLE`, `DELETE_GROUP`, `LOCK_USER`, `UNLOCK_USER`, `CHANGE_USER_ROLES` и др.), входов
(`LOCAL_LOGIN`, `REMOTE_LOGIN`, `REMOTE_OTP_CHECK`, `SUBSYSTEM_AUTH`, `X509_AUTH`) и сессий (`SSO_SESSION_CREATED`):

| Событие | Когда |
|---------|-------|
| `CHANGE_USER_ROLES` | Выдача/снятие ролей в UI (`details`: `added=`, `removed=`, `grantActions=` — списки id ролей) |
| `SSO_PASSWORD_LOGIN` | Шаг пароля SSO-входа (`details`: подсистема, `tempPassword=true` — вход с временным паролем) |
| `REMOTE_CHANGE_USER_ROLE` | `changeUserRole` через remote API (`details`: роль и подсистема) |
| `CHANGE_USER_OTP_OPTIONAL` | Включение/отключение обязательности MFA (`details`: `otpOptional=true/false`) |
| `CHANGE_USER_OTP_TYPE` | Смена типа OTP (`details`: `otpType=...`) |
| `PERSIST_OTP_MASTER_KEY` | Запись нового OTP master key пользователя (сброс `resetGoogleAuthMasterKey`, выдача ключа); сам ключ не логируется |
| `UPDATE_USER_DESCRIPTION` | Изменение описания/реквизитов пользователя через remote API |
| `UNLOCK_SUSPENDED_USER` | Разблокировка приостановленного пользователя (новый пароль не логируется) |
| `AUTO_LOCK_USER` | Автоблокировка по лимиту неудачных попыток (`details`: тип лимита и порог); `user:<SYSTEM>` |
| `VIEW_SMTP_PASSWORD` | Администратор открыл пароль SMTP-сервера на странице просмотра |

IP неудачных попыток входа дополнительно сохраняется в `unauthorised_access.ip_address` (вход в админку и шаг пароля SSO).

**Выкатка.** Изменились хранимые процедуры `get_user_login_status` (`get/get_user_login_status.sql`, новый параметр `i_ip_address`)
и `login_locked` (файл `stub/lockout_conditionnally.prc`; в `error_message` возвращается `ACCOUNT_LOCKED`, если вызов
заблокировал учётную запись): процедуры (`all-proc.sh`) и
WAR нужно выкатывать одновременно.

**Log injection.** В `docker/jetty/logback.xml` сообщение выводится как `%replace(%msg){'[\r\n\t]+', '_'}`, чтобы
пользовательский ввод в любых логах приложения не создавал поддельных записей. Стектрейс (`%ex`) не заменяется:
он многострочный по природе, выводится логбэком после сообщения и состоит из строк, сформированных JVM. Свой
logback-конфиг при запуске вне Docker должен использовать тот же `%replace`.

## SMTP (Email-уведомления)

SMTP-серверы настраиваются через веб-интерфейс администратора (`/smtpServer/list`), а не через файлы конфигурации.

## HOTP / Двухфакторная аутентификация

HOTP-провайдер настраивается через `superfly-spi`. Реализация подключается через Spring DI. По умолчанию используется `NullHOTPProvider` (2FA отключена).

Новые пользователи создаются с `is_otp_optional = 'N'` (миграция R1.7.7; существующие записи не меняются). Если у пользователя настроен ключ Google Authenticator, код обязателен независимо от этого флага. Принятый TOTP-код нельзя использовать повторно: шаг времени последнего принятого кода хранится в `users.otp_last_used_step`, код принимается только для шага выше сохранённого.

## Content-Security-Policy

Origin из `landingUrl` и `subsystemUrl` всех подсистем добавляются в директиву `form-action`, а `loginFormCssUrl` — в `style-src`.
Список кэшируется на 5 минут и сбрасывается сразу при правке подсистемы в админке. После сбоя загрузки (БД недоступна) в течение 10 секунд отдаётся базовая политика без обращений к БД.

- Остаточный риск: `style-src` глобальный — `loginFormCssUrl` любой подсистемы разрешает загрузку CSS с этого origin на **всех** страницах Superfly,
  не только на форме входа этой подсистемы. Указывайте в `loginFormCssUrl` только доверенные хосты.
- `Referrer-Policy: same-origin` отправляется на всех ответах основной цепочки: адреса вида `?subsystemToken=...&targetUrl=...` не попадают в `Referer` при переходах на другие origin.

## Reverse proxy и cookie

Docker-образ предполагает TLS-терминирующий прокси (nginx и т. п.) перед Jetty. Модуль Jetty `forwarded` (`ForwardedRequestCustomizer`)
**по умолчанию выключен** и включается переменной окружения `JETTY_TRUST_FORWARDED=true` (`docker/jetty/entrypoint.sh`). Тогда схема, хост
и IP клиента берутся из `Forwarded` / `X-Forwarded-Proto` / `X-Forwarded-For` / `X-Forwarded-Host`: прокси должен слать `Host`,
`X-Forwarded-Proto: https` и `X-Forwarded-For`, после чего `request.isSecure()` верен и заголовок HSTS выставляется
(Spring Security пишет его только для secure-запросов).

- Jetty не умеет ограничивать доверенные адреса прокси: заголовки принимаются от любого клиента. Включайте флаг **только когда порт 8080
  доступен исключительно доверенному прокси** (ограничьте на уровне сети или публикуйте порт на `127.0.0.1`). `compose.yml` по умолчанию
  публикует `${APP_PORT:-8080}` на всех интерфейсах, и `compose.production.yml` это не меняет; при прямом доступе и включённом флаге
  IP клиента (аудит, remote-auth, блокировки) и `isSecure` подделываются.
- Прокси обязан передавать исходный `Host` (nginx: `proxy_set_header Host $host;`). Wicket-формы защищены `SameOriginResourceIsolationPolicy`:
  браузеры без заголовков `Sec-Fetch-*` (старые) проверяются по `Origin`/`Referer` против `Host`, и при подменённом `Host` получат `403` на отправке форм.
- Без флага за прокси `getRemoteAddr()` — IP прокси, а `isSecure()`/HSTS по заголовкам не определяются. Cookie при этом всё равно Secure.
- `SSOSESSIONID` всегда отдаётся с `Secure`, `HttpOnly`, `SameSite=Lax`; `JSESSIONID` — `Secure`, `HttpOnly` (web.xml), без `;jsessionid=` в URL.
  Флага для отключения нет: браузеры принимают `Secure`-cookie на `http://localhost`, так что локальный запуск (`./dev-env.sh app`)
  работает; доступ по `http://<не-localhost>` без TLS работать не будет.

## Принятое поведение: пользователь без ролей

Пользователя без единой роли («незавершённого», например после сбоя между созданием и назначением ролей) может пересоздать **любая** подсистема
через RPC создания пользователя. Это сознательное поведение: у такого пользователя нет доступа ни к одной подсистеме, поэтому пересоздание
ничего не раскрывает; изолировать подсистемы друг от друга на этом уровне не требуется.

## Ограничение попыток входа (rate limit)

Считаются **неудачные** попытки шагов логина, при превышении — отказ без обращения к БД (не растёт `logins_failed`, поэтому с одного IP
нельзя заблокировать чужой аккаунт):

- админский логин: `LoginRateLimitFilter` на `/j_superfly_password_security_check`, `/j_superfly_otp_security_check`, `/j_superfly_otp_reset` — ответ `429` с `Retry-After`;
- SSO-логин: Wicket-страницы `SSOLoginPasswordPage` и `SSOLoginHOTPPage` (идут мимо `/j_superfly_*`) — сообщение об ошибке на форме
  «Too many failed login attempts». Счётчики общие с админским логином (один процесс, один `LoginAttemptLimiter`).

| Ключ | Лимит | Окно |
|------|-------|------|
| IP (отдельно для каждого шага) | 20 неудач, настраивается (`SUPERFLY_LOGIN_IP_LIMIT`, 0 = выключен) | 5 минут (фиксированное, от первой неудачи) |
| IP + username (без регистра, пробелов и диакритики — как collation БД) | 5 неудач | 5 минут |

- `SUPERFLY_LOGIN_IP_LIMIT` — переменная окружения или параметр `superfly-loginIpLimit`. Лимит пары IP + username (5) не отключается.
- Лимит пары (5) ниже порога блокировки аккаунта (6 неверных паролей), поэтому один IP упирается в блок раньше, чем блокирует аккаунт.
  Распределённый перебор с многих IP лимитом не закрывается.
- Успешный шаг сбрасывает только счётчик пары, счётчик IP не сбрасывается. Сбои инфраструктуры (`AuthenticationServiceException`) не считаются.
- Счётчики в памяти (Caffeine, до 100 000 записей) **на каждой ноде**: при N нодах без sticky-сессий эффективный лимит до N раз больше, перезапуск ноды его обнуляет.
- **Внимание: общий IP.** Лимит работает по `request.getRemoteAddr()`. За прокси без `JETTY_TRUST_FORWARDED=true` это IP прокси, и **все пользователи
  делят один лимит по IP**: 20 чужих неудач за 5 минут блокируют вход всем. Включите `JETTY_TRUST_FORWARDED=true` (с условиями выше) либо
  поднимите/отключите `SUPERFLY_LOGIN_IP_LIMIT`; пользователи за общим NAT тоже делят лимит.
- Не охвачены: remote-auth `/sso/check/*` (свой лимит на ошибки расшифровки и 3 попытки OTP на сессию; IP там — IP подсистемы), RPC
  `/remoting/sso.service/*` (вызывающий — доверенная подсистема) и `SSOChangePasswordPage` (задаёт новый пароль после успешной проверки временного, сам пароль не проверяет).
- В лог (WARN) пишутся шаг, IP и первые 8 hex SHA-256 от username; сам username и пароль не логируются.

## Переменные окружения

Подключение к БД в Docker-образе задаётся `DB_*` и `JETTY_PORT` (см. выше). Остальные параметры — через XML-конфиги Jetty и Spring.

## See Also

- [Установка и запуск](getting-started.md) — начальная настройка БД
- [Руководство по интеграции](integration-guide.md) — настройка клиентских приложений
