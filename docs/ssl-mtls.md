[← Руководство по интеграции](integration-guide.md) · [Back to README](../README.md)

# SSL / mTLS — настройка клиентского соединения

Описывает как Superfly-клиент устанавливает HTTPS-соединение к серверу, какие проверки выполняет JDK и как правильно их настроить.

---

## Как работает TLS-соединение: две независимые проверки

При каждом соединении JDK выполняет **два разных** шага верификации. Многие думают что это одно и то же — это не так.

### Шаг 1 — Certificate chain validation (TrustManager)

**Вопрос:** "Этому сертификату вообще можно доверять?"

JDK берёт сертификат сервера, смотрит кем он подписан (Issuer), находит этого CA в truststore и математически проверяет подпись. Если CA есть в truststore — цепочка доверия установлена.

```
Сервер прислал:  cert { CN=superfly-server, Issuer=myCA, ... }
Truststore:      содержит cert { CN=myCA }

JDK: подпись myCA на сертификате сервера валидна? → ✅ доверяем
```

Кастомный `AuthSSLX509TrustManager` из модуля `superfly-httpclient-hc5` оборачивает стандартный JDK TrustManager и дополнительно логирует информацию о сертификате — это полезно для отладки. Сама верификация цепочки выполняется делегатом (стандартный JDK).

### Шаг 2 — Hostname verification (HostnameVerifier)

**Вопрос:** "Этот сертификат выдан именно для того хоста, к которому мы подключаемся?"

JDK сравнивает hostname из URL с полями сертификата:

```
URL:      https://superfly.internal:8446/...
Cert CN:  superfly-server
Cert SAN: DNS:superfly.internal, DNS:superfly-server

Проверка: "superfly.internal" есть в SAN? → ✅ ok
```

Если сертификат содержит только `CN=superfly-server` без SAN, а мы подключаемся к `localhost`:

```
URL:      https://localhost:8446/...
Cert CN:  superfly-server
Cert SAN: (нет)

Проверка: "localhost" == "superfly-server"? → ❌ 
→ SSLHandshakeException: No name matching localhost found
```

**Важно:** шаг 2 происходит **после** шага 1. Даже если цепочка доверия установлена — JDK всё равно проверит hostname.

### Зачем нужна проверка hostname?

Защита от ситуации когда CA выдал легитимный сертификат другому сервису, и этот сервис (или атакующий, его скомпрометировавший) пытается выдать себя за нужный хост:

```
evil.internal получил cert { CN=evil.internal } от того же CA
Пытается выдать себя за superfly.internal

Без hostname check: TrustManager говорит ок (cert от доверенного CA) → уязвимость
С hostname check:   CN=evil.internal ≠ superfly.internal → отказ ✅
```

---

## Компоненты SSL в superfly-httpclient-hc5

После миграции на Apache HttpClient 5 SSL-хелперы живут в модуле `superfly-httpclient-hc5`
(пакет `com.payneteasy.httpclient.contrib.ssl`); отдельный модуль `superfly-httpclient-ssl`
и commons-coupled класс `AuthSSLProtocolSocketFactory` (для Apache Commons HttpClient 3.x) удалены.

| Класс | Назначение |
|-------|-----------|
| `JdkSslSocketFactoryBuilder` | Строит `SSLContext` (для HC5), `SSLSocketFactory` и `HostnameVerifier` из JKS-файлов |
| `AuthSSLX509TrustManager` | Обёртка над стандартным TrustManager с логированием сертификата |
| `AuthSSLX509KeyManager` | Обёртка над стандартным KeyManager с логированием |

### JdkSslSocketFactoryBuilder

Основной entry point для HC5 — `buildSslContext(...)`: собирает `SSLContext` из keystore + truststore,
который передаётся в `ApacheHC5HttpClient.Builder.sslContext(...)`.

```java
// SSLContext для HC5 (keyStore/trustStore — java.net.URL; любой может быть null)
SSLContext ssl = JdkSslSocketFactoryBuilder.buildSslContext(
        keyStoreUrl,   keyStorePassword,
        trustStoreUrl, trustStorePassword
);

// HostnameVerifier по CN вместо hostname (для dev / self-signed)
HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server");

// Legacy: SSLSocketFactory (если нужен напрямую, вне HC5)
SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
        keyStoreUrl, keyStorePassword, trustStoreUrl, trustStorePassword);
```

---

## Конфигурация SSL в ApacheHC5HttpClient

SSL/mTLS настраивается **один раз при создании** клиента через builder — HC5 использует
пул соединений с единым SSL-контекстом:

| Метод builder | Тип | Назначение |
|---------------|-----|-----------|
| `.sslContext(SSLContext)` | `SSLContext` | Какому CA доверяем + клиентский сертификат для mTLS (шаги 1). `null` → JVM default |
| `.hostnameVerifier(HostnameVerifier)` | `HostnameVerifier` | Проверка hostname (шаг 2). `null` → стандартная проверка |

```java
try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder()
        .sslContext(ssl)
        .hostnameVerifier(verifier)
        .build()) {
    // client.send(request, params)
}
```

> Поля `sslSocketFactory` / `hostnameVerifier` в per-request `HttpRequestParameters`
> игнорируются HC5 — SSL фиксируется на уровне пула при создании клиента.

---

## Сценарии использования

### Сценарий 1: HTTPS с публичным сертификатом (прод)

Сертификат выдан доверенным публичным CA (Let's Encrypt, DigiCert и т.д.), CN/SAN соответствует реальному hostname.

```java
// sslContext/hostnameVerifier не нужны — JDK использует системный truststore
ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build();
```

### Сценарий 2: HTTPS с кастомным CA, CN совпадает с hostname (прод с внутренним PKI)

```
Cert: CN=superfly.internal, SAN: DNS:superfly.internal
URL:  https://superfly.internal:8446/...
```

```java
SSLContext ssl = JdkSslSocketFactoryBuilder.buildSslContext(
        keyStoreUrl, keyStorePassword,
        trustStoreUrl, trustStorePassword  // truststore с кастомным CA
);

ApacheHC5HttpClient client = ApacheHC5HttpClient.builder()
        .sslContext(ssl)            // кастомный CA
        // hostnameVerifier не нужен — CN/SAN совпадает с hostname
        .build();
```

### Сценарий 3: mTLS, CN не совпадает с hostname (dev / localhost)

```
Cert: CN=superfly-server (без SAN)
URL:  https://localhost:8446/...
```

Стандартный hostname verifier откажет ("localhost" ≠ "superfly-server"). Решения:

**Вариант A — CN-based verifier (рекомендуется для dev):**

```java
SSLContext ssl = JdkSslSocketFactoryBuilder.buildSslContext(
        keyStoreUrl, keyStorePassword, trustStoreUrl, trustStorePassword);
HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server");

ApacheHC5HttpClient client = ApacheHC5HttpClient.builder()
        .sslContext(ssl)
        .hostnameVerifier(verifier)
        .build();
```

Верификатор проверяет CN сертификата вместо hostname. Безопасен при условии что приватный ключ CA под контролем (CA не скомпрометирован).

**Вариант B — перевыпустить сертификат с SAN (рекомендуется для прода):**

```bash
# Добавить в конфиг при генерации:
subjectAltName=DNS:superfly-server,DNS:localhost,IP:127.0.0.1
```

После этого стандартный verifier сработает корректно.

---

## Конфигурация в клиентском приложении (Spring)

Клиентское приложение собирает `ApacheHC5HttpClient` как Spring-бин (`IHttpClient`) и применяет
SSL, когда заданы пути к keystore/truststore. Типичный паттерн @Bean-фабрики:

```java
@Bean
public IHttpClient superflyHttpClient(SsoClientProperties cfg) throws Exception {
    ApacheHC5HttpClient.Builder builder = ApacheHC5HttpClient.builder();
    if (cfg.getKeyStoreUrl() != null) {
        builder.sslContext(JdkSslSocketFactoryBuilder.buildSslContext(
                cfg.getKeyStoreUrl(),   cfg.getKeyStorePassword(),
                cfg.getTrustStoreUrl(), cfg.getTrustStorePassword()));
        if (cfg.getExpectedServerCn() != null) {   // CN сервера; null → стандартный hostname verifier
            builder.hostnameVerifier(
                JdkSslSocketFactoryBuilder.buildCnHostnameVerifier(cfg.getExpectedServerCn()));
        }
    }
    return builder.build();   // AutoCloseable — Spring закроет пул на shutdown
}
```

Рекомендуемые параметры конфигурации приложения:

| Параметр | Описание |
|----------|---------|
| `keyStoreUrl` / `keyStorePassword` | Keystore с клиентским сертификатом и приватным ключом (mTLS) |
| `trustStoreUrl` / `trustStorePassword` | Truststore с сертификатом CA сервера |
| `expectedServerCn` | CN сертификата сервера; `null` = стандартный hostname verifier (прод с корректным SAN) |

Для прода с корректным SAN в сертификате — оставить `expectedServerCn` пустым.

---

## Keystore / truststore: как создать для разработки

```bash
# 1. Создать CA (корневой сертификат)
keytool -genkeypair -alias myCA -keyalg RSA -keysize 2048 \
        -dname "CN=myCA,C=RU" -validity 7300 \
        -keystore ca.jks -storepass changeit

# 2. Создать сертификат сервера
keytool -genkeypair -alias superfly-server -keyalg RSA -keysize 2048 \
        -dname "CN=superfly-server,C=RU" \
        -ext "SAN=DNS:superfly-server,DNS:localhost,IP:127.0.0.1" \
        -validity 7300 -keystore server.jks -storepass changeit

# 3. Подписать сертификат сервера через CA
keytool -certreq -alias superfly-server -keystore server.jks -storepass changeit | \
keytool -gencert -alias myCA -keystore ca.jks -storepass changeit \
        -ext "SAN=DNS:superfly-server,DNS:localhost,IP:127.0.0.1" \
        -validity 7300 | \
keytool -importcert -alias superfly-server -keystore server.jks -storepass changeit

# 4. Создать truststore для клиентов (только публичный cert CA)
keytool -exportcert -alias myCA -keystore ca.jks -storepass changeit | \
keytool -importcert -alias myCA -keystore cacert.jks -storepass changeit -noprompt
```

Сертификат созданный на шаге 3 имеет SAN с `localhost` → стандартный hostname verifier будет работать без `buildCnHostnameVerifier`.

---

## Диагностика

### "No name matching X found"

```
SSLHandshakeException: No name matching localhost found
```

Причина: cert CN ≠ hostname, SAN не задан или не включает нужный хост.

Решения:
1. Добавить `expectedServerCn` в конфиг (dev-workaround)
2. Перевыпустить сертификат с нужным SAN

### "PKIX path building failed"

```
SSLHandshakeException: PKIX path building failed
```

Причина: CA сервера нет в truststore клиента.

Решение: добавить CA в `cacert.jks`:
```bash
keytool -importcert -alias myCA -file ca.crt -keystore cacert.jks -storepass changeit
```

### "Certificate expired"

```
CertificateExpiredException: NotAfter
```

Решение: перевыпустить сертификат сервера.

### Включить подробный SSL лог

Для диагностики TLS-хендшейка на уровне JVM:
```
-Djavax.net.debug=ssl:handshake
```
