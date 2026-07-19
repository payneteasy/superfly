[← API Reference](api.md) · [Back to README](../README.md) · [Миграция EE8 / EE10 →](migration-client-ee8-ee10.md)

# Руководство по интеграции

Описывает, как подключить клиентское Java-приложение к Superfly как SSO-провайдеру.

## Выбор артефакта

| Стек приложения | Артефакт |
|-----------------|---------|
| Spring 6 + Jakarta EE 10 (`jakarta.servlet.*`) | `superfly-spring-security` |
| Spring 5 + Java EE 8 (`javax.servlet.*`) | `superfly-spring-security-ee8` |
| Общие типы без Servlet API | `superfly-spring-security-core` |
| Wicket 10 (Jakarta) | `superfly-wicket` |
| Wicket 8 (javax) | `superfly-wicket-ee8` |
| Без Spring Security | `superfly-client-ee10` или `superfly-client-ee8` |

## Быстрый старт (Jakarta EE 10 + Spring Security)

### 1. Добавить зависимость

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security</artifactId>
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
http://superfly-server:8080/
```

### Проверка учётных данных

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "secret",
  "subsystem": "MY_SUBSYSTEM"
}
```

Ответ: JSON с токеном сессии или ошибкой.

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

## Миграция между EE8 и EE10

Подробности в [руководстве по миграции](migration-client-ee8-ee10.md).

## See Also

- [Конфигурация](configuration.md) — настройка сервера Superfly
- [Миграция EE8 / EE10](migration-client-ee8-ee10.md) — переход на раздельные модули
