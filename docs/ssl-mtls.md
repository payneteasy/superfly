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

Кастомный `AuthSSLX509TrustManager` из модуля `superfly-httpclient-ssl` оборачивает стандартный JDK TrustManager и дополнительно логирует информацию о сертификате — это полезно для отладки. Сама верификация цепочки выполняется делегатом (стандартный JDK).

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

## Компоненты SSL в superfly-httpclient-ssl

Модуль `superfly-httpclient-ssl` предоставляет:

| Класс | Назначение |
|-------|-----------|
| `JdkSslSocketFactoryBuilder` | Строит `SSLSocketFactory` и `HostnameVerifier` из JKS-файлов |
| `AuthSSLX509TrustManager` | Обёртка над стандартным TrustManager с логированием сертификата |
| `AuthSSLX509KeyManager` | Обёртка над стандартным KeyManager с логированием |
| `AuthSSLProtocolSocketFactory` | Legacy: SSLSocketFactory для Apache Commons HttpClient 3.x |

### JdkSslSocketFactoryBuilder

Основной entry point для современного кода (используется совместно с `http-client-impl`).

```java
// Собрать SSLSocketFactory из keystore + truststore
SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
        keyStoreUrl,   keyStorePassword,
        trustStoreUrl, trustStorePassword
);

// Получить TrustManager (для диагностики или ручного использования)
X509TrustManager tm = JdkSslSocketFactoryBuilder.buildTrustManager(
        trustStoreUrl, trustStorePassword
);

// Получить HostnameVerifier по CN вместо hostname
HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server");
```

---

## Конфигурация HttpRequestParameters

`HttpRequestParameters` из `http-client-api` принимает SSL-параметры:

| Поле | Тип | Назначение |
|------|-----|-----------|
| `sslSocketFactory` | `SSLSocketFactory` | Контролирует какому CA доверяем (шаг 1) |
| `hostnameVerifier` | `HostnameVerifier` | Контролирует проверку hostname (шаг 2) |
| `trustManager` | `X509TrustManager` | Не используется в `HttpClientImpl` — поле присутствует для совместимости |

`HttpClientImpl` применяет оба поля к `HttpsURLConnection`:

```java
((HttpsURLConnection) connection).setSSLSocketFactory(parameters.getSslSocketFactory());
((HttpsURLConnection) connection).setHostnameVerifier(parameters.getHostnameVerifier());
```

---

## Сценарии использования

### Сценарий 1: HTTPS с публичным сертификатом (прод)

Сертификат выдан доверенным публичным CA (Let's Encrypt, DigiCert и т.д.), CN/SAN соответствует реальному hostname.

```java
HttpRequestParameters params = HttpRequestParameters.builder()
        .timeouts(timeouts)
        .build(); // sslSocketFactory и hostnameVerifier не нужны — JDK использует системный truststore
```

### Сценарий 2: HTTPS с кастомным CA, CN совпадает с hostname (прод с внутренним PKI)

```
Cert: CN=superfly.internal, SAN: DNS:superfly.internal
URL:  https://superfly.internal:8446/...
```

```java
SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
        keyStoreUrl, keyStorePassword,
        trustStoreUrl, trustStorePassword  // truststore с кастомным CA
);

HttpRequestParameters params = HttpRequestParameters.builder()
        .timeouts(timeouts)
        .sslSocketFactory(factory)  // кастомный CA
        // hostnameVerifier не нужен — CN совпадает с hostname
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
SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(...);
HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server");

HttpRequestParameters params = HttpRequestParameters.builder()
        .timeouts(timeouts)
        .sslSocketFactory(factory)
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

`SuperflySsoServiceConfig` управляет параметрами SSL на стороне клиента:

| Поле | Дефолт | Описание |
|------|--------|---------|
| `keyStoreResourceUrl` | `classpath:stores/paynet-local.jks` | Keystore с клиентским сертификатом и приватным ключом |
| `keyStorePassword` | `changeit` | Пароль keystore |
| `trustStoreResourceUrl` | `classpath:stores/cacert.jks` | Truststore с сертификатом CA |
| `trustStorePassword` | `changeit` | Пароль truststore |
| `expectedServerCn` | `superfly-server` | CN сертификата сервера; `null` = использовать стандартный hostname verifier |

`SpringUISuperflyClientConfiguration` автоматически применяет SSL когда `keyStoreResourceUrl != null`:

```java
if (ssoConfig.getKeyStoreResourceUrl() != null) {
    paramsBuilder.sslSocketFactory(buildSslSocketFactory());
    if (ssoConfig.getExpectedServerCn() != null) {
        paramsBuilder.hostnameVerifier(
            JdkSslSocketFactoryBuilder.buildCnHostnameVerifier(ssoConfig.getExpectedServerCn())
        );
    }
}
```

Для прода с корректным SAN в сертификате — установить `expectedServerCn: null` в YAML-конфиге.

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
