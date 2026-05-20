# Базовые правила проекта Superfly

> Автоопределённые конвенции из анализа кодовой базы. Редактировать по необходимости.

## Именование

- **Файлы/классы:** PascalCase (`UserPasswordEncoderImpl`, `SubsystemDao`)
- **Методы/переменные:** camelCase (`getSalt`, `userId`, `subsystemDao`)
- **Константы:** UPPER_SNAKE_CASE
- **Интерфейсы:** без префикса `I` (`PasswordEncoder`, `SaltSource`)
- **Реализации:** суффикс `Impl` (`UserPasswordEncoderImpl`)
- **DAO:** суффикс `Dao` (`SubsystemDao`, `SmtpServerDao`)
- **UI-модели:** префикс `UI` + суффикс по назначению (`UISubsystem`, `UISubsystemForList`, `UISubsystemForFilter`)
- **Пакеты:** `com.payneteasy.superfly.<module>.<layer>`

## Структура модулей

- `src/main/java` — исходный код
- `src/test/java` — тесты
- `src/main/resources` — конфиги Spring XML, logback, properties
- `src/main/webapp` — web-ресурсы (только в `superfly-web`)
- Слои: `dao`, `service`, `model`, `controller/mvc`, `spring`, `security`

## Обработка ошибок

- Кастомные исключения в пакете `api.exceptions` (например, `SsoDecryptException`, `UserNotFoundException`)
- Исключения — checked где API-контракт, unchecked для внутренних ошибок
- Не глотать исключения молча — логировать перед повторным бросом или возвращать `RoutineResult`

## Логирование

- SLF4J через `LoggerFactory.getLogger(ClassName.class)`
- Имя переменной: `log` или `logger` (встречаются оба)
- Production: JSON через strilog-json-encoder-shaded
- Уровни: `DEBUG` для детальных операций, `INFO` для бизнес-событий, `ERROR` для исключений

## Тесты

- JUnit 4 (`@Test`, `Assert.*`)
- Интеграционные тесты: наследование от `AbstractDaoTest`, инъекция через `@Autowired`
- **Не мокировать БД** — интеграционные тесты работают против реального MySQL
- Unit-тесты: прямое создание объектов, без Spring контекста
- Паттерн имени: `ClassNameTest`

## Инъекция зависимостей

- Setter injection с `@Autowired` (исторически преобладает)
- Constructor injection допустим для новых классов
- `@Component`, `@Service`, `@Repository` для автосканирования
- Conditional beans: `@OnPolicyCondition` (кастомная аннотация)

## База данных

- Только MySQL stored procedures через jdbc-proc (`JdbcProcedureFactory`)
- Никакого JPA/Hibernate/MyBatis
- Модели возвращаемых данных: `RoutineResult` для статусов операций
- SQL-миграции: в `superfly-sql/mi/` с именованием `R<version>/<description>.sql`

## EE8/EE10 разделение

- Код, зависящий от `javax.servlet.*` → модуль `*-ee8`
- Код, зависящий от `jakarta.servlet.*` → модуль `*-ee10` или без суффикса
- Core-модули (без servlet зависимости) → суффикс `-core`
- Не смешивать javax и jakarta в одном модуле
