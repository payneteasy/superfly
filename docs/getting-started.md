[Back to README](../README.md) · [Конфигурация →](configuration.md)

# Установка и запуск

## Требования

| Компонент | Версия |
|-----------|--------|
| Java | 21+ |
| Maven | 3.9+ (или `./mvnw`) |
| MariaDB | 10.3+ (рекомендуется) |
| Docker | любая (для быстрого старта) |
| Git | любая |

> **MariaDB vs MySQL:** SQL-схема использует синтаксис MariaDB (в частности `GRANT ... IDENTIFIED BY`
> и таблицу `groups`). Для MySQL 8+ потребуется адаптация миграций.

---

## Быстрый старт (локальная разработка)

### 1. Запустить MariaDB в Docker

```bash
docker run -d --name superfly-db \
  -p 3344:3306 \
  -e MYSQL_ROOT_PASSWORD=1234 \
  mariadb:10.3 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_general_ci

# Ждём готовности (10–20 сек)
until mysql -h 127.0.0.1 -P 3344 -u root -p1234 -e "SELECT 1" 2>/dev/null; do
  echo "Waiting for MariaDB..."; sleep 2
done
```

Переменные окружения миграционных скриптов:

| Переменная | По умолчанию | Описание |
|------------|-------------|---------|
| `SSO_DB_HOST` | `localhost` | Хост БД |
| `SSO_DB_PORT` | `3344` | Порт |
| `SSO_DB_ROOT` | `root` | Root-пользователь |
| `SSO_DB_ROOT_PASSWORD` | `1234` | Root-пароль |
| `SSO_DB_USERNAME` | `sso` | Пользователь приложения |
| `SSO_DB_PASSWORD` | `123sso123` | Пароль приложения |
| `SSO_DB_DATABASE` | `sso` | Имя базы |

### 2. Накатить миграции

```bash
cd superfly-sql/mi
bash all_mi.sh
```

Скрипт последовательно применяет все версии R1.0.0 → R1.7.x. Логи пишутся в `<version>/target/*.log`.

Для нестандартного порта:

```bash
SSO_DB_PORT=3343 bash all_mi.sh
```

### 3. Сбросить пароль admin

Миграции создают пользователя `admin` с солью (для pcidss-политики). В dev-режиме используется
политика `none` (см. шаг 4), поэтому нужно сбросить хеш на SHA-256 без соли:

```bash
# SHA-256("123admin123") = c9cddf4205cb7b6e6e918dc967bb6505d1447a2a499ef165a8c03784170307ee
mysql -h 127.0.0.1 -P 3344 -u sso -p123sso123 sso -e "
UPDATE users
SET user_password = 'c9cddf4205cb7b6e6e918dc967bb6505d1447a2a499ef165a8c03784170307ee',
    salt = NULL
WHERE user_name = 'admin';
"
```

Пользователь admin должен иметь:

```sql
SELECT user_name, user_password, salt FROM users WHERE user_name = 'admin';
-- user_password = c9cddf42...  salt = NULL
SELECT COUNT(*) FROM user_roles ur JOIN users u ON u.user_id = ur.user_user_id WHERE u.user_name = 'admin';
-- >= 1
```

### 4. Добавить dev-политику в override-web.xml

Файл `superfly-web/src/test/resources/jetty/override-web.xml` должен содержать:

```xml
<context-param>
    <param-name>superfly-policy</param-name>
    <param-value>none</param-value>
</context-param>
```

**Почему это нужно:** `web.xml` устанавливает `superfly-policy=pcidss` → `RandomStoredSaltSource`.
При логине она читает соль из БД и вычисляет хеш с ней. Если `salt=NULL` — аутентификация падает.
Политика `none` активирует `NullSaltSource` → SHA-256 без соли. Файл `override-web.xml` используется
только `Start.java` и не попадает в production WAR.

### 5. Собрать проект

```bash
./mvnw -DskipTests -pl superfly-web -am package
```

### 6. Запустить

**Через IDEA:** Run-конфиг `Start` из `.run/Start.run.xml`  
- Main class: `com.payneteasy.superfly.Start`
- Модуль: `superfly-web`

**Через Maven:**

```bash
./mvnw -pl superfly-web -DskipTests jetty:run
```

Приложение ждёт нажатия клавиши для остановки.

### 7. Проверить

Откройте `http://localhost:8085/superfly/`

| Поле | Значение |
|------|---------|
| Логин | `admin` |
| Пароль | `123admin123` |

---

## Настройка подключения к БД

Файл `superfly-web/src/main/webapp/WEB-INF/jetty-web.xml` — подключение по умолчанию:

```xml
<Set name="url">jdbc:mysql://localhost:3344/sso?characterEncoding=utf8
    &amp;useInformationSchema=true&amp;noAccessToProcedureBodies=false
    &amp;useLocalSessionState=true</Set>
<Set name="username">sso</Set>
<Set name="password">123sso123</Set>
```

---

## Устранение неисправностей

| Симптом | Причина | Решение |
|---------|---------|---------|
| `Connection refused` на 3344 | MariaDB не запущена | Шаг 1 |
| `Table 'sso.users' doesn't exist` | Миграции не накатаны | Шаг 2 |
| Логин неверен (правильный пароль) | salt != NULL или policy=pcidss | Шаги 3–4 |
| `NullPointerException` при старте | Нет JNDI datasource | Проверить `jetty-web.xml` |
| Wicket error после логина | Нет `user_role_actions` | Проверить шаг 3: роли накатаны? |

---

## Следующие шаги

- [Конфигурация](configuration.md) — настройка портов, БД, параметров приложения
- [Руководство по интеграции](integration-guide.md) — подключение клиентских приложений
- [Миграция EE8/EE10](migration-client-ee8-ee10.md) — javax/jakarta split
