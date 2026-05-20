[← Доменная модель](domain-model.md) · [Back to README](../README.md) · [Руководство по интеграции →](integration-guide.md)

# API Reference

Superfly предоставляет два типа API:

| Тип | Путь | Формат | Назначение |
|-----|------|--------|-----------|
| [RPC API](#rpc-api-ssoservice) | `/sso.service/{method}` | JSON / XML | Аутентификация, управление пользователями |
| [REST API](#rest-api) | `/check-password`, `/check-otp` | JSON | Проверка учётных данных |

---

## Аутентификация

### RPC API (`/sso.service/**`)

Требует роль `ROLE_SUBSYSTEM`. Поддерживается:
- **Bearer Token**: `Authorization: Bearer {subsystem_token}`
- **Basic Auth**: `Authorization: Basic {base64(user:password)}`

Токен подсистемы (`subsystemToken`) задаётся при регистрации подсистемы в UI.

### REST API (`/check-password`, `/check-otp`)

Обязателен заголовок:
```
Authorization: Bearer {subsystem_token}
```

---

## RPC API: `/sso.service`

Каждый метод — это `POST /{methodName}` с JSON-телом.

**Content-Type**: `application/json` (по умолчанию) или `application/xml`

**Обработка ошибок**: все исключения возвращаются как `ExceptionWrapper` с кодом ошибки:
```json
{ "className": "com.payneteasy.superfly.api.exceptions.UserNotFoundException",
  "message": "User 'john' not found" }
```

---

### Аутентификация

#### `POST /sso.service/authenticate`

Проверяет логин/пароль. Возвращает сессию с ролями и действиями.

**Request:**
```json
{
  "username": "john",
  "password": "secret",
  "authRequestInfo": {
    "subsystemIdentifier": "MY_APP"
  }
}
```

**Response:** `SSOUser | null`
```json
{
  "name": "john",
  "sessionId": 42,
  "otpType": "none",
  "isOtpOptional": false,
  "actionsMap": {
    "OPERATOR": ["READ_ORDERS", "UPDATE_STATUS"]
  },
  "preferences": {}
}
```

---

#### `POST /sso.service/pseudoAuthenticate`

Аутентификация без проверки пароля (для доверенных систем). Параметры аналогичны `authenticate`.

---

#### `POST /sso.service/exchangeSubsystemToken`

Обменивает SSO-токен (redirect-based flow) на сессию пользователя.

**Request:**
```json
{ "subsystemToken": "abc123..." }
```

**Response:** `SSOUser | null`

---

#### `POST /sso.service/checkOtp`

Проверяет OTP-код для двухфакторной аутентификации.

**Request:**
```json
{
  "username": "john",
  "otpEncrypted": "...",
  "sessionId": 42
}
```

**Response:** `boolean` — `true` если код верный

**Исключения:** `SsoDecryptException`

---

#### `POST /sso.service/hasOtpMasterKey`

Проверяет, настроен ли Google Authenticator для пользователя.

**Request:**
```json
{ "username": "john" }
```

**Response:** `boolean`

---

#### `POST /sso.service/touchSessions`

Обновляет время активности сессий (предотвращает таймаут).

**Request:**
```json
{ "sessionIds": [42, 43, 44] }
```

**Response:** `void`

---

### Управление пользователями

#### `POST /sso.service/registerUser`

Регистрирует нового пользователя. Пароль должен соответствовать политике безопасности.

**Request:**
```json
{
  "username": "john",
  "password": "SecurePass1!",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "organization": "Acme Corp",
  "subsystemHint": "MY_APP",
  "otpType": "none",
  "isOtpOptional": false,
  "isPasswordTemp": false,
  "roleGrants": [
    { "roleName": "OPERATOR", "subsystemName": "MY_APP" }
  ]
}
```

**Response:** `void`

**Исключения:** `UserExistsException`, `PolicyValidationException`, `BadPublicKeyException`, `MessageSendException`

---

#### `POST /sso.service/getUserDescription`

Возвращает профиль пользователя.

**Request:**
```json
{ "username": "john" }
```

**Response:** `UserDescription | null`
```json
{
  "username": "john",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "organization": "Acme Corp",
  "otpType": "none",
  "isOtpOptional": false,
  "publicKey": null
}
```

---

#### `POST /sso.service/updateUserDescription`

Обновляет профиль пользователя.

**Request:** `UserDescription` (те же поля, что и в `getUserDescription`)

**Исключения:** `UserNotFoundException`, `BadPublicKeyException`

---

#### `POST /sso.service/changeTempPassword`

Меняет временный пароль (при первом входе).

**Request:**
```json
{
  "username": "john",
  "newPassword": "NewSecurePass1!"
}
```

**Исключения:** `PolicyValidationException`

---

#### `POST /sso.service/resetPassword`

Сбрасывает пароль пользователя администратором.

**Request:**
```json
{
  "username": "john",
  "newPassword": "TempPass1!",
  "isPasswordTemp": true
}
```

**Исключения:** `UserNotFoundException`, `PolicyValidationException`

---

#### `POST /sso.service/getUserStatuses`

Возвращает статусы нескольких пользователей (заблокирован, приостановлен и т.д.).

**Request:**
```json
{ "usernames": ["john", "jane"] }
```

**Response:** `List<UserStatus>`

---

#### `POST /sso.service/getUsersWithActions`

Возвращает пользователей с их действиями для указанной подсистемы.

**Request:**
```json
{ "subsystemIdentifier": "MY_APP" }
```

**Response:** `List<SSOUserWithActions>`
```json
[
  {
    "username": "john",
    "email": "john@example.com",
    "actions": ["READ_ORDERS", "UPDATE_STATUS"]
  }
]
```

---

#### `POST /sso.service/completeUser`

Завершает процесс регистрации пользователя (активирует аккаунт).

---

#### `POST /sso.service/changeUserRole`

Меняет роль пользователя в подсистеме.

**Request:**
```json
{
  "username": "john",
  "subsystemName": "MY_APP",
  "newRoleName": "ADMIN"
}
```

---

### OTP / Google Authenticator

#### `POST /sso.service/updateUserOtpType`

Меняет тип OTP для пользователя.

**Request:**
```json
{
  "username": "john",
  "otpType": "google_auth"
}
```

---

#### `POST /sso.service/updateUserIsOtpOptionalValue`

Устанавливает, является ли OTP обязательным.

**Request:**
```json
{
  "username": "john",
  "isOtpOptional": true
}
```

---

#### `POST /sso.service/resetGoogleAuthMasterKey`

Сбрасывает и перегенерирует мастер-ключ Google Authenticator.

**Request:**
```json
{
  "username": "john",
  "masterKeyEncrypted": "..."
}
```

**Response:** `String` — новый мастер-ключ (зашифрованный)

**Исключения:** `UserNotFoundException`, `SsoDecryptException`

---

#### `POST /sso.service/getUrlToGoogleAuthQrCode`

Возвращает `otpauth://` URI для QR-кода.

**Request:**
```json
{
  "username": "john",
  "masterKeyEncrypted": "..."
}
```

**Response:** `String` — `otpauth://totp/Superfly:john?secret=...`

---

### Синхронизация данных

#### `POST /sso.service/sendSystemData`

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

#### `POST /sso.service/getEvents`

Long-polling для получения событий (изменения прав, блокировки).

**Request:**
```json
{
  "lastEventTime": "2025-01-01T00:00:00+0300",
  "waitTimeMs": 30000
}
```

**Response:** `List<SSOEvent>`
```json
[
  {
    "eventId": 1,
    "eventTime": "2025-05-20T12:00:00+0300",
    "eventTypeCode": "USER_LOCKED",
    "eventData": "{\"username\":\"john\"}"
  }
]
```

---

## REST API

### `POST /check-password/{subsystemName}/{username}`

Проверяет зашифрованный пароль пользователя.

**Request:**
```
POST /check-password/MY_APP/john
Authorization: Bearer {subsystem_token}
Content-Type: application/json

{
  "username": "john",
  "passwordEncrypted": "..."
}
```

**Response (200 OK):**
```json
{
  "username": "john",
  "sessionToken": "session-abc123",
  "otpRequired": false
}
```

**HTTP-коды:** `200 OK` · `400 Bad Request` · `401 Unauthorized` · `500 Internal Server Error`

---

### `POST /check-otp/{subsystemName}/{username}`

Проверяет OTP-код для завершения двухфакторной аутентификации.

**Request:**
```
POST /check-otp/MY_APP/john
Authorization: Bearer {subsystem_token}
Content-Type: application/json

{
  "username": "john",
  "otpEncrypted": "...",
  "sessionToken": "session-abc123"
}
```

**Response (200 OK):**
```json
{
  "username": "john",
  "sessionToken": "session-abc123",
  "result": "OK"
}
```

---

## Иерархия исключений

```
SsoException
  ├── SsoAuthException          — ошибки аутентификации
  ├── SsoClientException        — ошибки клиента (400)
  ├── SsoServerException        — ошибки сервера (500)
  ├── SsoConnectionException    — проблемы соединения
  ├── SsoDecryptException       — ошибки шифрования
  ├── UserNotFoundException     — пользователь не найден
  ├── UserExistsException       — пользователь уже существует
  ├── PolicyValidationException — нарушение политики паролей
  ├── BadPublicKeyException     — некорректный публичный ключ
  └── MessageSendException      — ошибка отправки email
```

---

## See Also

- [Доменная модель](domain-model.md) — структура сущностей
- [Руководство по интеграции](integration-guide.md) — как подключить клиентское приложение
