[← Конфигурация](configuration.md) · [Back to README](../README.md) · [API Reference →](api.md)

# Доменная модель

## Ключевые сущности

```
Subsystem (подсистема)
  ├── Role (роль)  ──────────────── Group (группа действий)
  │     └── Action (действие)           └── Action
  └── User (пользователь)
        └── AuthSession (сессия)
              └── AuthRole → AuthAction
```

Все права пользователя всегда **контекстно-зависимы от подсистемы** — одному пользователю
можно дать разные роли в разных подсистемах.

---

## Subsystem — подсистема

Внешнее приложение, зарегистрированное в Superfly. Каждая подсистема имеет собственный
набор ролей и действий.

| Поле | Тип | Описание |
|------|-----|---------|
| `id` | long | Первичный ключ |
| `name` | String | Уникальный идентификатор (используется в API-вызовах) |
| `title` | String | Человекочитаемое название |
| `callbackUrl` | String | URL для callback-уведомлений при изменении прав |
| `sendCallbacks` | boolean | Включить ли отправку callbacks |
| `subsystemToken` | String | Токен для redirect-based SSO |
| `subsystemUrl` | String | Базовый URL приложения |
| `landingUrl` | String | URL страницы после успешного SSO |
| `loginFormCssUrl` | String | CSS для кастомизации формы входа |
| `privateKey` / `publicKey` | String | Ключевая пара для шифрования токенов |
| `encryptionAlgorithm` | String | Алгоритм шифрования токенов |
| `allowListUsers` | boolean | Разрешить получение списка пользователей подсистемы |
| `smtpServer` | UISmtpServerForFilter | SMTP-сервер для email-уведомлений |

---

## User — пользователь

| Поле | Тип | Описание |
|------|-----|---------|
| `id` | Long | Первичный ключ |
| `username` | String | Логин (уникальный) |
| `password` | String | Хэш пароля |
| `email` | String | Email |
| `name` / `surname` | String | Имя и фамилия |
| `organization` | String | Организация |
| `secretQuestion` / `secretAnswer` | String | Секретный вопрос для восстановления |
| `salt` | String | Соль для хэширования пароля |
| `publicKey` | String | Публичный ключ пользователя |
| `otpType` | OTPType | Тип двухфакторной аутентификации |
| `isOtpOptional` | boolean | Можно ли пропустить OTP |

### Статус пользователя (в UIUserForList)

| Поле | Описание |
|------|---------|
| `accountLocked` | Аккаунт заблокирован (превышен лимит неудачных попыток) |
| `accountSuspended` | Аккаунт приостановлен администратором |
| `loginsFailed` | Число неудачных попыток входа подряд |
| `lastLoginDate` | Дата последнего успешного входа |
| `nextOtpCounter` | Счётчик HOTP |

---

## Role — роль

Роль принадлежит конкретной подсистеме. Пользователю назначается роль в контексте подсистемы.

| Поле | Тип | Описание |
|------|-----|---------|
| `roleId` | long | Первичный ключ |
| `roleName` | String | Имя роли |
| `principalName` | String | Spring Security principal (обычно совпадает с `roleName`) |
| `subsystemId` | long | Подсистема, к которой относится роль |

---

## Action — действие

Атомарное разрешение внутри подсистемы. Пример: `READ_ORDERS`, `ADMIN`, `VIEW_REPORTS`.

| Поле | Тип | Описание |
|------|-----|---------|
| `actionId` | long | Первичный ключ |
| `actionName` | String | Имя действия (идентификатор) |
| `actionDescription` | String | Человекочитаемое описание |
| `subsystemId` | long | Подсистема |
| `logAction` | boolean | Логировать ли использование |

---

## Group — группа действий

Удобный способ объединить набор действий для массового назначения.

| Поле | Тип | Описание |
|------|-----|---------|
| `id` | long | Первичный ключ |
| `name` | String | Имя группы |
| `subsystemId` | long | Подсистема |

Группа содержит набор действий. Роли можно назначать действия как по одному, так и через группу.

---

## AuthSession — сессия аутентификации

Создаётся при успешном входе. Содержит все роли и действия, активные в данный момент.

| Поле | Тип | Описание |
|------|-----|---------|
| `sessionId` | Long | Уникальный ID сессии |
| `username` | String | Пользователь |
| `otpTypeId` | Long | Тип OTP |
| `isOtpOptional` | boolean | OTP опционален |
| `roles` | List\<AuthRole\> | Роли с вложенными действиями |

```java
// Структура AuthSession
AuthSession {
    sessionId: 42L,
    username: "john",
    roles: [
        AuthRole {
            roleName: "OPERATOR",
            actions: [
                AuthAction { actionName: "READ_ORDERS" },
                AuthAction { actionName: "UPDATE_STATUS" }
            ]
        }
    ]
}
```

---

## Перечисления

### OTPType — тип двухфакторной аутентификации

| Значение | Код | Описание |
|----------|-----|---------|
| `NONE` | `"none"` | OTP отключён |
| `GOOGLE_AUTH` | `"google_auth"` | Google Authenticator (TOTP) |

### Policy — политика паролей

| Значение | Описание |
|----------|---------|
| `NONE` | Без политики |
| `PCIDSS` | Минимум 8 символов, спецсимволы, история паролей |

### UserLoginStatus

| Значение | Код | Описание |
|----------|-----|---------|
| `SUCCESS` | `"Y"` | Успешный вход |
| `FAILED` | `"N"` | Неудачная попытка |
| `TEMP_PASSWORD` | `"T"` | Вход с временным паролем |

### LockoutType — причина блокировки

`SESSION` · `ROLES` · `PASSWORD` · `HOTP`

---

## RoutineResult — результат операции

Большинство write-операций возвращают `RoutineResult`:

| Статус | Значение |
|--------|---------|
| `"OK"` | Успех |
| `"duplicate"` | Запись уже существует |
| `"fail"` | Ошибка (см. `errorMessage`) |

---

## UI-модели: соглашения о суффиксах

| Суффикс | Назначение | Пример |
|---------|-----------|--------|
| `ForList` | Пагинированные списки в таблицах | `UIUserForList` |
| `ForView` | Детальный просмотр одной записи | `UIRoleForView` |
| `ForFilter` | Выпадающие списки и фильтры | `UISubsystemForFilter` |
| `ForCheckbox` | Чекбоксы при массовом назначении прав | `UIActionForCheckbox` |
| `ForCreate` | Форма создания | `UIUserForCreate` |
| `WithRolesAndActions` | Полная иерархия прав | `UIUserWithRolesAndActions` |
| `Details` | Расширенное редактирование | `UIUserDetails` |

Эти модели — DTO между DAO-слоем и Wicket-UI. Никогда не используются в REST API.

---

## See Also

- [API Reference](api.md) — типы запросов/ответов в REST API
- [Руководство по интеграции](integration-guide.md) — как назначать роли через API
