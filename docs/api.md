[← Доменная модель](domain-model.md) · [Back to README](../README.md) · [Руководство по интеграции →](integration-guide.md)

# API Reference

Superfly предоставляет два типа API. Формат везде только JSON (XML-сериализация и Spring HTTP invoker удалены).

| Тип | Путь | Назначение |
|-----|------|-----------|
| [RPC API](#rpc-api-remotingssoservice) | `/remoting/sso.service/{method}` | Аутентификация, управление пользователями, события |
| [Remote-auth](#remote-auth-ssocheck) | `/sso/check/check-password/{subsystem}/{user}`, `/sso/check/check-otp/{subsystem}/{user}` | Проверка зашифрованного пароля и OTP внешней системой |

Пути указаны относительно context path приложения (в Docker-образе это `/`, при локальном запуске `/superfly`).

---

## Аутентификация

### RPC API (`/remoting/sso.service/**`)

Требует роль `ROLE_SUBSYSTEM`. Подсистема аутентифицируется одним из способов:

- **Заголовки** `X-Subsystem-Name: {subsystem}` и `X-Subsystem-Token: {subsystemToken}` (так ходит `SSOHttpServiceApiClient`);
- **Клиентский сертификат** (mTLS, X509).

Токен подсистемы (`subsystemToken`) задаётся при регистрации подсистемы в UI.

### Remote-auth (`/sso/check/**`)

Путь доступен без сессии, токен проверяет сам контроллер. Обязателен заголовок:

```
Authorization: Bearer {subsystem_token}
```

Любой другой путь под `/sso/check/` отвечает `404` (`type: NOT_FOUND`, см. [Ошибки](#ошибки)).

---

## RPC API: `/remoting/sso.service`

Каждый метод — `POST /remoting/sso.service/{methodName}` с JSON-телом (`Content-Type: application/json`).
Имена методов и типы запросов соответствуют интерфейсу `SSOService` из `superfly-remote-api`;
поля тела — поля классов `com.payneteasy.superfly.api.request.*`. Даты — `yyyy-MM-dd'T'HH:mm:ssZ`.

**Статусы ответа:** `200` — успех; `202` — метод бросил исключение (тело — `ExceptionWrapper`);
`500` — сбой самого вызова (неизвестный метод, невалидное тело), тело тоже `ExceptionWrapper`.
Клиент `SSOHttpServiceApiClient` разбирает `ExceptionWrapper` при любом статусе, кроме `200`.

**Обработка ошибок** — тело `ExceptionWrapper`:
```json
{ "exceptionClass": "com.payneteasy.superfly.api.UserNotFoundException",
  "message": "User 'john' not found",
  "detailMessage": "com.payneteasy.superfly.api.UserNotFoundException: User 'john' not found" }
```

### Подмена подсистемы

Если вызывающий — подсистема (`ROLE_SUBSYSTEM`), то `subsystemIdentifier` (в т.ч. в `authRequestInfo`), `subsystemHint`,
`GetEventsRequest.subsystemName` и `roleGrants[].subsystemIdentifier` (при `detectSubsystemIdentifier = false`)
должны быть `null` или совпадать с её именем. Иначе ответ `202` с `ExceptionWrapper`
`com.payneteasy.superfly.api.exceptions.SsoAuthException` (`Subsystem identifier does not match the authenticated subsystem`),
на сервере пишется WARN. Для локального UI ограничения нет.

---

### Аутентификация

#### `authenticate`

Проверяет логин/пароль. Возвращает сессию с ролями и действиями.

**Request:**
```json
{
  "username": "john",
  "password": "secret",
  "authRequestInfo": {
    "ipAddress": "10.0.0.5",
    "sessionInfo": "Mozilla/5.0",
    "subsystemIdentifier": "MY_APP"
  }
}
```

**Response:** `SSOUser | null`
```json
{
  "name": "john",
  "sessionId": "42",
  "otpType": "NONE",
  "isOtpOptional": false,
  "actionsMap": {
    "OPERATOR": [
      { "name": "READ_ORDERS", "loggingNeeded": false },
      { "name": "UPDATE_STATUS", "loggingNeeded": true }
    ]
  },
  "preferences": {}
}
```

---

#### `pseudoAuthenticate`

Аутентификация без проверки пароля (для доверенных систем).

**Request:** `{ "username": "john", "subsystemIdentifier": "MY_APP" }`

---

#### `exchangeSubsystemToken`

Обменивает SSO-токен (redirect-based flow) на сессию пользователя.

**Request:** `{ "subsystemToken": "abc123..." }`

**Response:** `SSOUser | null`

---

#### `checkOtp`

Проверяет OTP-код для двухфакторной аутентификации.

**Request:**
```json
{ "userName": "john", "code": "123456", "otpType": "GOOGLE_AUTH", "isOtpOptional": false }
```

**Response:** `boolean` — `true` если код верный

**Исключения:** `SsoDecryptException`

---

#### `hasOtpMasterKey`

Проверяет, настроен ли Google Authenticator для пользователя.

**Request:** `{ "username": "john" }`

**Response:** `boolean`

---

#### `touchSessions`

Обновляет время активности сессий (предотвращает таймаут).

**Request:** `{ "sessionIds": [42, 43, 44] }`

**Response:** `void`

---

### Управление пользователями

#### `registerUser`

Регистрирует нового пользователя. Пароль должен соответствовать политике безопасности.
Тело — `UserRegisterRequest` (поля `UserDescription` плюс `password`, `subsystemHint`, `roleGrants`).

**Request:**
```json
{
  "username": "john",
  "password": "SecurePass1!",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "organization": "Acme Corp",
  "otpType": "NONE",
  "isOtpOptional": false,
  "subsystemHint": "MY_APP",
  "roleGrants": [
    { "subsystemIdentifier": "MY_APP", "principalName": "OPERATOR" }
  ]
}
```

**Response:** `void`

**Исключения:** `UserExistsException`, `PolicyValidationException`, `BadPublicKeyException`, `MessageSendException`

---

#### `getUserDescription`

Возвращает профиль пользователя.

**Request:** `{ "username": "john" }`

**Response:** `UserDescription | null`
```json
{
  "username": "john",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "organization": "Acme Corp",
  "publicKey": null,
  "otpType": "NONE",
  "isOtpOptional": false
}
```

---

#### `updateUserDescription`

Обновляет профиль пользователя.

**Request:** `{ "userDescription": { ...поля UserDescription, как в getUserDescription... } }`

**Исключения:** `UserNotFoundException`, `BadPublicKeyException`

---

#### `changeTempPassword`

Меняет временный пароль (при первом входе).

**Request:** `{ "username": "john", "newPassword": "NewSecurePass1!" }`

**Исключения:** `PolicyValidationException`

---

#### `resetPassword`

Сбрасывает пароль пользователя администратором.

**Request:** `{ "username": "john", "password": "TempPass1!", "sendByEmail": false }`

**Исключения:** `UserNotFoundException`, `PolicyValidationException`

---

#### `getUserStatuses`

Возвращает статусы нескольких пользователей.

**Request:** `{ "userNames": ["john", "jane"] }`

**Response:** `List<UserStatus>` — `username`, `accountLocked`, `lastLoginDate`, `loginsFailed`, `lastFailedLoginDate`, `lastFailedLoginIp`

---

#### `getUsersWithActions`

Возвращает пользователей с их действиями для указанной подсистемы.

**Request:** `{ "subsystemIdentifier": "MY_APP" }`

**Response:** `List<SSOUserWithActions>`
```json
[
  {
    "name": "john",
    "email": "john@example.com",
    "actions": [ { "name": "READ_ORDERS", "loggingNeeded": false } ]
  }
]
```

---

#### `completeUser`

Завершает процесс регистрации пользователя.

**Request:** `{ "username": "john" }`

---

#### `changeUserRole`

Меняет роль пользователя в подсистеме.

**Request:** `{ "username": "john", "newRole": "ADMIN", "subsystemHint": "MY_APP" }`

---

### OTP / Google Authenticator

#### `updateUserOtpType`

**Request:** `{ "username": "john", "otpType": "google_auth" }`

---

#### `updateUserIsOtpOptionalValue`

Устанавливает, является ли OTP необязательным.

**Request:** `{ "username": "john", "isOtpOptional": true }`

---

#### `resetGoogleAuthMasterKey`

Сбрасывает и перегенерирует мастер-ключ Google Authenticator.

**Request:** `{ "username": "john" }`

**Response:** `String` — новый мастер-ключ

**Исключения:** `UserNotFoundException`, `SsoDecryptException`

---

#### `getUrlToGoogleAuthQrCode`

Возвращает `otpauth://` URI для QR-кода.

**Request:** `{ "secretKey": "...", "issuer": "Superfly", "accountName": "john" }`

**Response:** `String` — `otpauth://totp/...`

---

### Синхронизация данных

#### `sendSystemData`

Регистрирует список действий подсистемы (синхронизирует Actions в Superfly).
Вызывается при старте приложения.

**Request:**
```json
{
  "subsystemIdentifier": "MY_APP",
  "actionDescriptions": [
    { "name": "READ_ORDERS",   "description": "Просмотр заказов" },
    { "name": "UPDATE_STATUS", "description": "Изменение статуса" }
  ]
}
```

**Response:** `void`

---

#### `getEvents`

Long-polling для получения событий. Подсистема определяется по аутентификации запроса; `subsystemName` в теле должен быть `null` или совпадать
с именем аутентифицированной подсистемы (см. [Подмена подсистемы](#подмена-подсистемы)).
Возвращаются события с `eventId` больше `lastEventId`, в порядке возрастания `eventId`; `lastEventId = null` — с начала.
Для следующего запроса передайте максимальный `eventId` из последнего ответа (`eventTime` курсором быть не может).
`waitTimeMs` ограничен сервером 75 секундами — socket timeout клиента должен быть больше.
События без подсистемы клиентам не отдаются.

**Request:**
```json
{ "lastEventId": 41, "waitTimeMs": 30000 }
```

**Response:** `List<SSOEvent>`
```json
[
  {
    "eventId": 1,
    "eventTime": "2025-05-20T12:00:00+0300",
    "eventTypeCode": "PASSWORD_RESET",
    "eventData": "john"
  }
]
```

`PASSWORD_RESET` пишется отдельной записью на каждую подсистему, в которой у пользователя есть роли.

---

## Remote-auth (`/sso/check`)

Проверка пароля и OTP внешней системой, которая не использует `SSOService`. Пароль и OTP
передаются зашифрованными открытым ключом подсистемы.

### Шифрование

Пара ключей (RSA 4096) генерируется в админке на странице создания/редактирования подсистемы
(«Generate new RSA key pair», затем сохранить форму); там же показывается открытый ключ в PEM. Закрытый ключ в UI не выводится.
Алгоритм хранится в колонке `subsystems.encryption_algorithm`:

| Алгоритм | Padding | Кому |
|----------|---------|------|
| `RSA_OAEP` | `RSA/ECB/OAEPPadding`: OAEP, хеш SHA-256, MGF1-SHA256, label пустой | новые ключи из админки |
| `RSA` | PKCS#1 v1.5 | старые ключи, пока подсистема не перегенерировала ключ |
| `EC` | — | не поддерживается |

Шифротекст передаётся в base64url (RFC 4648 §5), открытый текст — UTF-8.

**Переход существующей подсистемы на OAEP:** на странице редактирования подсистемы нажать
«Generate new RSA key pair», сохранить форму (алгоритм станет `RSA_OAEP`) и перевести клиента на новый ключ и OAEP.
Пока ключ не перегенерирован, подсистема остаётся на `RSA` (PKCS#1).

Пример на Java — параметры OAEP нужно указывать явно: имя `OAEPWithSHA-256AndMGF1Padding` без
`OAEPParameterSpec` даёт MGF1 с SHA-1 и расходится с OpenSSL/WebCrypto:

```java
byte[] der = Base64.getMimeDecoder().decode(publicKeyPem
        .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", ""));
PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));

Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
cipher.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec(
        "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
String passwordEncrypted = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
```

То же через OpenSSL (`subsystem-public.pem` — открытый ключ подсистемы):

```bash
printf '%s' "$PASSWORD" \
  | openssl pkeyutl -encrypt -pubin -inkey subsystem-public.pem \
      -pkeyopt rsa_padding_mode:oaep -pkeyopt rsa_oaep_md:sha256 -pkeyopt rsa_mgf1_md:sha256 \
  | openssl base64 -A | tr '+/' '-_' | tr -d '='
```

### Лимит ошибок расшифровки

Не более **20 ошибок расшифровки в минуту на подсистему** (защита от подбора через padding oracle
с валидным токеном). Сверх лимита запрос отклоняется без попытки расшифровки:
`400`, `type: BAD_REQUEST`, `title: Decryption failed` — тот же ответ, что и при обычной ошибке расшифровки.

### `POST /sso/check/check-password/{subsystemName}/{username}`

**Request:**
```
POST /sso/check/check-password/MY_APP/john
Authorization: Bearer {subsystem_token}
Content-Type: application/json

{ "username": "john", "passwordEncrypted": "..." }
```

**Response (200 OK):**
```json
{ "username": "john", "sessionToken": "9b2f...-uuid", "otpRequired": false }
```

`sessionToken` живёт 5 минут, привязан к подсистеме и пользователю и одноразовый при успешной проверке OTP
(после 3 неверных OTP аннулируется).

### `POST /sso/check/check-otp/{subsystemName}/{username}`

**Request:**
```
POST /sso/check/check-otp/MY_APP/john
Authorization: Bearer {subsystem_token}
Content-Type: application/json

{ "username": "john", "otpEncrypted": "...", "sessionToken": "9b2f...-uuid" }
```

**Response (200 OK):**
```json
{ "username": "john", "sessionToken": "9b2f...-uuid", "result": "SUCCESS" }
```

`result` — `SUCCESS` или `BAD_USER_OR_PASSWORD_OR_OTP`.

### Ошибки

Тело ошибки — `{ "type": "...", "title": "...", "detail": "...", "errorId": "<uuid>" }`:

| `type` | HTTP | Причина |
|--------|------|---------|
| `UNAUTHORIZED` | 401 | нет/неверный Bearer-токен, неизвестная подсистема |
| `BAD_REQUEST` | 400 | невалидный JSON, не заполнены поля, логин в пути и теле не совпадает, ошибка расшифровки (в т.ч. по лимиту) |
| `BAD_USER_OR_PASSWORD_OR_OTP` | 400 | неверный логин/пароль, неверная или просроченная сессия OTP |
| `USER_SHOULD_CHANGE_PASSWORD` | 400 | у пользователя временный пароль |
| `NOT_FOUND` | 404 | неизвестный путь под `/sso/check/` (`title: Not found`, `detail: Unknown endpoint`) |
| `INTERNAL_ERROR` | 500 | внутренняя ошибка |

Ошибки отдаются в JSON без заголовка `Accept`, с `Accept: */*` или `application/json`.

---

## Иерархия исключений

Все наследуются от `SsoException extends RuntimeException`.

```
SsoException
  ├── SsoClientException            — ошибки клиента (4xx)
  │     ├── SsoBadRequestException, SsoUnauthorizedException, SsoForbiddenException,
  │     │   SsoNotFoundException, SsoConflictException
  │     ├── UserExistsException       — пользователь уже существует
  │     ├── PolicyValidationException — нарушение политики паролей
  │     ├── BadPublicKeyException     — некорректный публичный ключ
  │     └── MessageSendException      — ошибка отправки email
  ├── SsoServerException            — ошибки сервера (5xx)
  ├── SsoAuthException, SsoUserException, SsoDataException, SsoSystemException, SsoParseException
  ├── SsoConnectionException        — проблемы соединения
  └── SsoDecryptException           — ошибки шифрования
```

`UserNotFoundException` (наследник `SsoException`) лежит в `com.payneteasy.superfly.api`, не в `exceptions`.

---

## See Also

- [Доменная модель](domain-model.md) — структура сущностей
- [Руководство по интеграции](integration-guide.md) — как подключить клиентское приложение
- [SSO HTTP Client](sso-http-client.md) — клиент для RPC API
