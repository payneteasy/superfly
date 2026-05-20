# Архитектура: Clean Architecture

## Обзор

Superfly реализует Clean Architecture через Maven-модули: каждый модуль — это слой
или адаптер с явными зависимостями. Бизнес-логика (`superfly-service`) не зависит от
деталей доставки (Wicket, Spring MVC, конкретный Servlet API). SPI-интерфейсы
(`superfly-spi`) — порты внутреннего слоя, а `superfly-client-ee8` / `superfly-client-ee10` —
типичные адаптеры к конкретной среде выполнения.

Паттерн выбран потому, что разграничение уже заложено в структуре: интерфейсы
отделены от реализаций, EE8/EE10-адаптеры изолированы, интеграционные тесты направлены
против реальной инфраструктуры, а не моков.

## Обоснование выбора

- **Тип проекта:** SSO-сервер с чёткими доменными операциями (аутентификация, авторизация, HOTP)
- **Стек:** Java 21, Spring 6, Wicket 10, MySQL stored procedures
- **Ключевой фактор:** Поддержка нескольких servlet API (EE8/EE10) требует явного Adapter Pattern;
  бизнес-логика не должна знать, в какой среде она запущена

## Структура модулей (слои)

```
┌─────────────────────────────────────────────────────────────────┐
│  FRAMEWORKS & DRIVERS (внешний слой)                            │
│                                                                  │
│  superfly-web              — Wicket UI + Spring MVC Controllers  │
│  superfly-client-ee8       — javax.servlet адаптер              │
│  superfly-client-ee10      — jakarta.servlet адаптер            │
│  superfly-spring-security-ee8  — Spring Security (EE8)          │
│  superfly-spring-security      — Spring Security (EE10)         │
│  superfly-wicket / -ee8    — Wicket компоненты                  │
│  superfly-sql              — SQL-миграции, stored procedures     │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│  INTERFACE ADAPTERS                                              │
│                                                                  │
│  superfly-remote-api       — REST/RPC API контракты             │
│  superfly-client-web-security — HTTP security адаптер           │
│  superfly-client-opt       — клиент с опциональными адаптерами  │
│  superfly-httpclient-ssl   — SSL mutual auth адаптер            │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│  APPLICATION LAYER                                               │
│                                                                  │
│  superfly-service          — Use cases: аутентификация,         │
│                              авторизация, управление польз.     │
│  superfly-spring-security-core — Spring Security integration    │
│  superfly-client-core      — Core клиент без servlet зависимости│
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│  DOMAIN LAYER (внутренний слой, без внешних зависимостей)       │
│                                                                  │
│  superfly-spi              — Порты/интерфейсы (HOTPProvider,    │
│                              SaltGenerator и др.)               │
│  superfly-spi-support      — Реализации SPI-интерфейсов         │
│  superfly-common           — Shared domain utilities            │
│  superfly-crypto           — Шифрование (независимо от стека)   │
└─────────────────────────────────────────────────────────────────┘
```

**Структура внутри модуля `superfly-service`:**

```
src/main/java/com/payneteasy/superfly/
├── dao/            # DAO-интерфейсы + реализации (jdbc-proc)
├── service/        # @Service классы — use cases
│   └── impl/
├── model/          # Доменные модели
│   └── ui/         # UI-специфичные view models (UIEntity*)
├── password/       # Пароль: SaltSource, PasswordEncoder
├── hotp/           # HOTP/OTP логика
├── email/          # Email-уведомления
├── lockout/        # Политика блокировки
├── policy/         # Политики паролей, доступа
├── notification/   # Стратегии уведомлений
└── spring/         # Spring config, условия (@OnPolicyCondition)
```

## Правила зависимостей

```
Domain Layer → ничего (pure Java, никаких Spring/Servlet/DB зависимостей)
Application Layer → Domain Layer только
Interface Adapters → Application + Domain (реализуют интерфейсы)
Frameworks & Drivers → Interface Adapters + Application
```

- ✅ `superfly-web` зависит от `superfly-service` и `superfly-remote-api`
- ✅ `superfly-service` зависит от `superfly-spi` и `superfly-common`
- ✅ `superfly-client-ee8` зависит от `superfly-client-core` (добавляет javax.servlet)
- ❌ `superfly-spi` НЕ должен зависеть от `superfly-service` или `superfly-web`
- ❌ `superfly-service` НЕ должен знать о javax.servlet или jakarta.servlet
- ❌ Нельзя смешивать `javax.servlet.*` и `jakarta.servlet.*` в одном модуле
- ❌ Нельзя добавлять зависимость от модуля более высокого слоя

## Коммуникация между слоями

- **Веб → Сервис:** Spring DI — инъекция `@Service` в Wicket Page/Panel через `@SpringBean`
- **Сервис → DAO:** инъекция `@Repository` через Spring; DAO реализует интерфейс через jdbc-proc
- **DAO → БД:** исключительно stored procedures через `JdbcProcedureFactory` (не JPA/JDBC напрямую)
- **EE8/EE10 адаптеры:** реализуют интерфейсы из `superfly-client-core` под конкретный Servlet API
- **Транзакции:** только на уровне `@Service` (аннотация `@Transactional`), не в DAO и не в веб-слое

## Ключевые принципы

1. **Направление зависимостей — внутрь.** Внешние слои зависят от внутренних, не наоборот.
2. **Servlet-независимость core.** Весь бизнес-код живёт в модулях без javax/jakarta зависимости.
3. **Интерфейс перед реализацией.** DAO, сервисы и SPI-расширения объявляются как интерфейсы.
4. **Только stored procedures.** Нет JPA/Hibernate/MyBatis; всё через jdbc-proc и хранимые процедуры MySQL.
5. **Интеграционные тесты против реальной БД.** Моки для БД запрещены — см. `superfly-integration-test`.
6. **UI-модели изолированы.** Классы `UI*ForList`, `UI*ForFilter` — только для веб-слоя, не для сервисного.

## Примеры кода

### DAO-интерфейс и реализация через jdbc-proc

```java
// superfly-service: интерфейс (Application Layer)
public interface UserDao {
    List<UIUserForList> getUsersForList(long subsystemId, int startFrom, int count);
    RoutineResult createUser(UIUser user);
}

// Реализация через jdbc-proc (Infrastructure/Adapter)
@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcProcedureFactory factory;

    @Autowired
    public UserDaoImpl(JdbcProcedureFactory factory) {
        this.factory = factory;
    }

    public List<UIUserForList> getUsersForList(long subsystemId, int startFrom, int count) {
        return factory.create(GetUsersForListProcedure.class)
                      .execute(subsystemId, startFrom, count);
    }
}
```

### Service использует интерфейс DAO (не реализацию)

```java
// superfly-service: Application Layer
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private UserDao userDao;        // инъекция интерфейса, не реализации
    private PasswordEncoder encoder;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public RoutineResult createUser(String username, String password) {
        String encoded = encoder.encode(password, username);
        return userDao.createUser(new UIUser(username, encoded));
    }
}
```

### Wicket Page не содержит бизнес-логики

```java
// superfly-web: Frameworks Layer
public class ListUsersPage extends AuthenticatedWebPage {
    @SpringBean
    private UserService userService;  // инъекция из Application Layer

    public ListUsersPage() {
        add(new UserListPanel("users", userService::getUsersForList));
    }
    // Никакой бизнес-логики — только делегирование сервису
}
```

### EE8-адаптер изолирует javax.servlet

```java
// superfly-client-ee8: Adapter Layer (javax.servlet зависимость изолирована здесь)
public class SuperflyHttpServletRequestWrapper
        extends javax.servlet.http.HttpServletRequestWrapper {

    private final SuperflyAuthenticationToken token;

    // Адаптирует javax.servlet API к внутреннему контракту
    @Override
    public Principal getUserPrincipal() {
        return token.getPrincipal();
    }
}
```

## Анти-паттерны

- ❌ Вызов DAO напрямую из Wicket Page — только через `@Service`
- ❌ `@Transactional` на методах DAO или Wicket Panel
- ❌ Импорт `javax.servlet.*` в модулях без суффикса `-ee8`
- ❌ Импорт `jakarta.servlet.*` в модулях с суффиксом `-ee8`
- ❌ JPA/Hibernate/MyBatis аннотации в любом модуле — только jdbc-proc
- ❌ Бизнес-логика в UI-классах (Wicket Page, Panel, Component)
- ❌ Мокирование БД в интеграционных тестах — только реальная MySQL
- ❌ Прямые SQL-запросы через `JdbcTemplate` в обход jdbc-proc (кроме схемных скриптов)
- ❌ Доменные модели с Wicket/Spring MVC аннотациями
