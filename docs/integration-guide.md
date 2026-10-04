[← API Reference](api.md) · [Back to README](../README.md) · [Миграция EE8 / EE10 →](migration-client-ee8-ee10.md)

# Руководство по интеграции

Описывает, как подключить клиентское Java-приложение к Superfly как SSO-провайдеру.

## Выбор артефакта

| Стек приложения | Артефакт |
|-----------------|---------|
| Spring 6 + Jakarta EE 10 (`jakarta.servlet.*`) | `superfly-spring-security-ee10` |
| Spring 5 + Java EE 8 (`javax.servlet.*`, JDK 21) | `superfly-spring-security-ee8` |
| Общие типы без Servlet API | `superfly-spring-security-core` |
| Wicket 10 (Jakarta) | `superfly-wicket` |
| Wicket 8 (javax) | `superfly-wicket-ee8` |
| Без Spring Security | `superfly-client-ee10` или `superfly-client-ee8` |

## Быстрый старт (Jakarta EE 10 + Spring Security)

### 1. Добавить зависимость

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security-ee10</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

### 2. Настроить Spring Security

```xml
<!-- applicationContext.xml или Java-конфиг -->
<bean id="superflyAuthProvider"
      class="com.payneteasy.superfly.security.SuperflyUsernamePasswordAuthenticationProvider">
    <property name="superflyService" ref="superflyService"/>
    <property name="subsystemIdentifier" value="MY_SUBSYSTEM"/>
</bean>

<security:authentication-manager>
    <security:authentication-provider ref="superflyAuthProvider"/>
</security:authentication-manager>
```

### 3. Добавить фильтры сессий

```xml
<bean id="sessionBindFilter"
      class="com.payneteasy.superfly.security.SSOUserSessionBindFilter"/>
```

## Режимы аутентификации

### No-redirect режим

Пользователь вводит логин/пароль в форму самого приложения. Приложение обращается
к Superfly по HTTP для проверки учётных данных.

```
Пользователь → Форма логина в приложении → Superfly (HTTP) → ответ → сессия
```

### Redirect-based SSO

Приложение перенаправляет пользователя на Superfly-сервер для аутентификации,
после чего Superfly перенаправляет обратно с токеном.

```
Пользователь → Приложение → Redirect → Superfly UI → аутентификация → Redirect назад
```

## HTTP API

Если Spring Security не используется, можно работать напрямую через HTTP API.

### Базовый URL

```
https://superfly-server/remoting/sso.service/{method}
```

Запросы — `POST` с JSON-телом и заголовками `X-Subsystem-Name` / `X-Subsystem-Token`
(или клиентский сертификат). Проверка учётных данных — метод `authenticate`:

```http
POST /remoting/sso.service/authenticate
Content-Type: application/json
X-Subsystem-Name: MY_SUBSYSTEM
X-Subsystem-Token: {subsystemToken}

{ "username": "user", "password": "secret",
  "authRequestInfo": { "subsystemIdentifier": "MY_SUBSYSTEM" } }
```

Ответ: `SSOUser` с ролями и действиями (`null`, если логин/пароль неверны) или `ExceptionWrapper` при ошибке.
Из Java используйте [`SSOHttpServiceApiClient`](sso-http-client.md). Внешним системам без `SSOService`
предназначены `/sso/check/check-password` и `/sso/check/check-otp` с шифрованием пароля — см. [API Reference](api.md#remote-auth-ssocheck).

## Подсистемы (Subsystems)

Каждое приложение регистрируется в Superfly как **подсистема** с уникальным идентификатором.
Права пользователей назначаются на уровне подсистемы.

1. Войти в Superfly UI → Subsystems → Create
2. Задать идентификатор (`SUBSYSTEM_IDENTIFIER`)
3. Указать тот же идентификатор в конфигурации клиента (`subsystemIdentifier`)

## Роли и действия

Superfly использует модель: **пользователь → роль → набор действий**.

- **Action** (действие) — конкретное разрешение (например, `READ_ORDERS`)
- **Role** (роль) — набор действий (например, `OPERATOR`)
- Пользователю назначается роль в рамках подсистемы

Действия можно описать через XML-дескриптор и зарегистрировать автоматически:

```java
// Spring компонент для автоматической регистрации действий
@Component
public class MyActionsSource implements ActionsSource {
    public List<String> getActions() {
        return List.of("READ_ORDERS", "WRITE_ORDERS", "ADMIN");
    }
}
```

## Интеграция с Wicket

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-wicket</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

```java
// WicketApplication.java
@Override
protected void init() {
    super.init();
    setRootRequestMapper(new PageInterceptingRequestMapper(
        getRootRequestMapper(),
        new PageInterceptingRequestMapperLogic(...)
    ));
}
```

## HOTP (двухфакторная аутентификация)

Для включения HOTP подключите `SuperflyOTPAuthenticationProvider`:

```xml
<bean id="otpAuthProvider"
      class="com.payneteasy.superfly.security.SuperflyOTPAuthenticationProvider">
    <property name="superflyService" ref="superflyService"/>
</bean>
```

## Subsystem isolation (2.0)

Методы `SSOService`, работающие с пользователем по имени, ограничены подсистемой вызывающего
(определяется по токену подсистемы): `checkOtp`, `hasOtpMasterKey`, `updateUserOtpType`,
`changeTempPassword`, `getUserDescription`, `resetGoogleAuthMasterKey`,
`updateUserIsOtpOptionalValue`, `updateUserDescription`, `resetPassword`, `completeUser`,
`getUserStatuses`. Пользователь должен иметь хотя бы одну роль в подсистеме вызывающего,
иначе ответ такой же, как для несуществующего пользователя.

- Пользователи с ролью в подсистеме `superfly` (админка) недоступны через RPC всегда, даже если
  у них есть роль и в подсистеме вызывающего. Учётки админки и подсистем должны быть раздельными.
- `exchangeSubsystemToken`: токен одноразовый, живёт 30 секунд и обменивается только подсистемой,
  для которой выдан (вызывающий определяется по токену подсистемы). Чужой, просроченный, уже
  использованный токен и токен заблокированного пользователя дают `null`; неудачный обмен чужим
  токеном его не сжигает. Заблокированному пользователю токен не выдаётся.
- `getUserStatuses` без списка имён (`null`) возвращает пустой список; имена с запятой игнорируются.
- `changeTempPassword` меняет пароль только пока он временный (`is_password_temp='Y'`).
- `resetPassword` проверяет новый пароль по password policy.

Перед выкаткой найдите учётки с ролями и в `superfly`, и в других подсистемах: через RPC они
перестанут быть доступны.

```sql
select u.user_name, group_concat(distinct s.subsystem_name)
  from users u
  join user_roles ur on ur.user_user_id = u.user_id
  join roles r on r.role_id = ur.role_role_id
  join subsystems s on s.ssys_id = r.ssys_ssys_id
 group by u.user_id
having sum(s.subsystem_name = 'superfly') > 0 and count(distinct s.subsystem_name) > 1;
```

## Миграция между EE8 и EE10

Подробности в [руководстве по миграции](migration-client-ee8-ee10.md).

## See Also

- [Конфигурация](configuration.md) — настройка сервера Superfly
- [Миграция EE8 / EE10](migration-client-ee8-ee10.md) — переход на раздельные модули, breaking changes
- [SSO HTTP Client](sso-http-client.md) — клиент RPC API
