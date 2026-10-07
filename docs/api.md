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

RPC stateless: сессия (`JSESSIONID`) не создаётся, заголовки нужны в каждом запросе; повтор с cookie без `X-Subsystem-*` не проходит.

### Remote-auth (`/sso/check/**`)

Путь доступен без сессии, токен проверяет сам контроллер. Обязателен заголовок:

```
Authorization: Bearer {subsystem_token}
```

Неполный или неизвестный путь под `/sso/check/check-password/` и `/sso/check/check-otp/` отвечает `404` (`type: NOT_FOUND`, см. [Ошибки](#ошибки)).
Любой другой путь под `/sso/check/` закрыт Spring Security (`denyAll`): без сессии — редирект на `/login`, с сессией — `403`.

---

## RPC API: `/remoting/sso.service`

Каждый метод — `POST /remoting/sso.service/{methodName}` с JSON-телом (`Content-Type: application/json`).
Имена методов и типы запросов соответствуют интерфейсу `SSOService` из `superfly-remote-api`;
поля тела — поля классов `com.payneteasy.superfly.api.request.*`. Даты — `yyyy-MM-dd'T'HH:mm:ssZ`.

**Accept:** ответ всегда `application/json`. Отсутствующий заголовок, `*/*`, `application/*` и списки, в которых
есть JSON, принимаются. Иначе (например `text/xml`) — `406` с пустым телом; проверка идёт до вызова метода,
побочных эффектов нет.

**Статусы ответа:** `200` — успех; `202` — метод бросил исключение (тело — `ExceptionWrapper`);
`406` — неподдерживаемый `Accept`;
`500` — сбой самого вызова (неизвестный метод, невалидное тело), тело тоже `ExceptionWrapper`.
Клиент `SSOHttpServiceApiClient` разбирает `ExceptionWrapper` при любом статусе, кроме `200`.

**Обработка ошибок** — тело `ExceptionWrapper`. Класс и сообщение отдаются только для исключений контракта
(`UserExistsException`, `PolicyValidationException`, `BadPublicKeyException`, `MessageSendException`,
`UserNotFoundException`, `SsoDecryptException`, `SsoAuthException`, `SsoUserException`, `SsoSystemException`,
`SsoDataException`); `detailMessage` всегда `null`:
```json
{ "exceptionClass": "com.payneteasy.superfly.api.UserNotFoundException",
  "message": "User 'john' not found",
  "detailMessage": null }
```
Любая другая ошибка (SQL/DAO, NPE, невалидный JSON, неизвестный метод) приходит обезличенной; `errorId` — UUID,
под которым исключение записано в серверный лог:
```json
{ "exceptionClass": "com.payneteasy.superfly.api.exceptions.SsoServerException",
  "message": "Internal server error, errorId: 3f0c1c8e-7a52-4b1e-9d0e-5a2f6f3d9c11",
  "detailMessage": null }
```
Исключения вне RPC-контроллера (`GlobalExceptionHandler`) — `500`, `{"error": "Internal server error", "errorId": "<uuid>"}`.

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

Токен одноразовый, живёт 30 секунд и обменивается только подсистемой, для которой выдан.
Иначе (чужой, просроченный, использованный токен, заблокированный пользователь) — `null`.

**Response:** `SSOUser | null`

---

#### `checkOtp`

Проверяет OTP-код для двухфакторной аутентификации.

**Request:**
```json
{ "userName": "john", "code": "123456", "otpType": "GOOGLE_AUTH", "isOtpOptional": false }
```

**Response:** `CheckOtpResult` — в HTTP-теле `{ "status": "SUCCESS" }`.

> **Несовместимо со старыми клиентами.** Раньше метод возвращал `boolean` (тело `true`/`false`); теперь — объект
> `CheckOtpResult`. Клиент (`SSOHttpServiceApiClient` и собственные реализации) и сервер обновляйте вместе.
> `/sso/check/check-otp` (remote-auth) не изменился.

| `status` | Значение |
|----------|---------|
| `SUCCESS` | Код верный; шаг времени сохранён, повторно этот код не пройдёт |
| `INVALID` | Неверный или некорректный (не 6 цифр) код; также неизвестный пользователь и пользователь, недоступный вызывающей подсистеме |
| `ALREADY_USED` | Код этого шага (или более позднего) уже использован |
| `CLOCK_SKEW` | Код относится к шагам ±2..±3 (по 30 с) от текущего — часы устройства расходятся с сервером. Код не принят, шаг не сохранён. Пользователю: синхронизировать время на устройстве и ввести новый код |
| `LOCKED` | Учётная запись заблокирована, код не проверялся; блокировка бессрочная (до разблокировки администратором), времени до снятия нет. Тот же статус получает попытка, которая сама привела к блокировке |

Принимается код текущего шага и ±1 шаг, каждый код — один раз. Каждая неудача (`INVALID`, `CLOCK_SKEW`, `ALREADY_USED`) —
неудачная OTP-попытка: счётчик растёт до порога `superfly-max-otp-failed` (см. [Конфигурацию](configuration.md#hotp--двухфакторная-аутентификация)).
Подробные статусы стоит показывать пользователю только после успешного шага пароля.

**Исключения:** `SsoDecryptException`

---

#### `hasOtpMasterKey`

Проверяет, настроен ли Google Authenticator для пользователя.

**Request:** `{ "username": "john" }`

**Response:** `boolean` — `true` только если есть **активный** ключ. Ключ, выданный `resetGoogleAuthMasterKey` и ещё не
подтверждённый (pending), не учитывается.

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

Генерирует новый мастер-ключ Google Authenticator и сохраняет его как **pending** (`users.otp_pending_master_key`).
Активный ключ не меняется: вход продолжает работать по старому, пока новый не подтверждён через `confirmOtpMasterKey`.
Повторный вызов перезаписывает pending-ключ. Неподтверждённый ключ удаляется, если администратор сбрасывает OTP
или пользователь настраивает ключ на SSO-странице (запись активного ключа); после этого `confirmOtpMasterKey` вернёт `INVALID`,
нужен новый `resetGoogleAuthMasterKey`. Перешифровка активного ключа при входе pending не затрагивает.

**Request:** `{ "username": "john" }`

**Response:** `String` — новый (pending) мастер-ключ

**Исключения:** `UserNotFoundException`, `SsoDecryptException`

---

#### `confirmOtpMasterKey`

Подтверждает pending-ключ, выданный `resetGoogleAuthMasterKey`, кодом, сгенерированным из него. При успехе pending-ключ
становится активным.

**Request:** `{ "username": "john", "code": "123456" }`

**Response:** `CheckOtpResult`

| `status` | Значение |
|----------|---------|
| `SUCCESS` | Ключ активирован. Код подтверждения помечен использованным: тем же кодом войти нельзя (`ALREADY_USED`) |
| `INVALID` | Неверный код; нет pending-ключа; неизвестный пользователь; pending-ключ заменён конкурентным `resetGoogleAuthMasterKey` |
| `CLOCK_SKEW` | Код вне окна ±1 шаг, но близко к нему; pending-ключ сохраняется |
| `ALREADY_USED` | Код этого шага уже использован |
| `LOCKED` | Учётная запись заблокирована (в том числе этой попыткой) |

Каждая неудача — неудачная OTP-попытка (общий счётчик и порог `superfly-max-otp-failed`); успешное подтверждение счётчик
неудачных попыток **не сбрасывает**. Повторный `confirmOtpMasterKey`
после успеха — тоже неудача (pending-ключа уже нет → `INVALID`), поэтому клиент должен защититься от двойного сабмита формы.

**Исключения:** `SsoDecryptException`

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
Чтобы при старте не переигрывать историю, начните с курсора из [`getLastEventId`](#getlasteventid).
`waitTimeMs` ограничен сервером 75 секундами — socket timeout клиента должен быть больше.
События без подсистемы клиентам не отдаются.
Событие отдаётся не сразу, а спустя ~5 секунд после создания (горизонт стабильности): событие, созданное ещё не закоммиченной транзакцией,
получает `eventId` раньше более позднего закоммиченного — без задержки курсор перепрыгнул бы через него. Long-polling учитывает это
автоматически: запрос вернётся, как только событие пройдёт горизонт (если хватит `waitTimeMs`).

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

#### `getLastEventId`

Возвращает максимальный `eventId` событий вызывающей подсистемы (подсистема — по аутентификации запроса); `0`, если событий нет.
События других подсистем не учитываются; события младше горизонта стабильности (~5 секунд, см. [`getEvents`](#getevents)) не учитываются —
они будут получены следующим `getEvents`. Используется как стартовый `lastEventId` для `getEvents`, когда история не нужна:
события, созданные после вызова, будут получены.

**Request:** тело `null` (аргументов нет)

**Response:** `Long`, например `42`

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
| `NOT_FOUND` | 404 | неполный/неизвестный путь под `/sso/check/check-password/` или `/sso/check/check-otp/` (`title: Not found`, `detail: Unknown endpoint`) |
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
