[Back to README](../README.md) · [Конфигурация →](configuration.md)

# Установка и запуск

## Требования

| Компонент | Версия |
|-----------|--------|
| Java | 21+ |
| Maven | 3.9+ (или используйте `./mvnw`) |
| MySQL | 8.0+ |
| Git | любая |

## Сборка из исходников

```bash
git clone https://github.com/payneteasy/superfly.git
cd superfly
./mvnw -DskipTests package
```

Для полной сборки с тестами (требует запущенный MySQL):

```bash
./mvnw package
```

## Подготовка базы данных

### 1. Создать схему и пользователя MySQL

```sql
CREATE DATABASE sso CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sso'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON sso.* TO 'sso'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Применить миграции

Миграции находятся в `superfly-sql/mi/`. Применяются последовательно по версиям:

```bash
cd superfly-sql
# Применить миграцию начальной схемы
mysql -u sso -p sso < mi/R1.0.0/R1.0.0_SSO_ROOT.sql
mysql -u sso -p sso < mi/R1.0.0/R1.0.0_SSO.sql
mysql -u sso -p sso < mi/R1.0.0/R1.0.0_SSO_DML.sql
# ... и последующие версии R1.1.0, R1.2.0, R1.3.0
```

Для скрипта автоматического применения всех миграций:

```bash
cd superfly-sql
./functions.sh
```

## Настройка подключения к БД

Отредактируйте `superfly-web/src/main/webapp/WEB-INF/jetty-web.xml`:

```xml
<Set name="url">jdbc:mysql://localhost:3306/sso?characterEncoding=utf8
    &amp;useInformationSchema=true&amp;noAccessToProcedureBodies=false
    &amp;useLocalSessionState=true&amp;serverTimezone=Europe/Moscow</Set>
<Set name="username">sso</Set>
<Set name="password">your_password</Set>
```

## Запуск

### Через Maven (разработка)

```bash
./mvnw -pl superfly-web jetty:run
```

### Развёртывание WAR-файла

```bash
# Собрать WAR
./mvnw -DskipTests package
# Развернуть superfly-web/target/superfly-web-*.war на Tomcat/Jetty/Wildfly
```

## Первый вход

После запуска откройте `http://localhost:8080`.

Учётные данные по умолчанию (из начальных DML-миграций):

| Поле | Значение |
|------|---------|
| Логин | `admin` |
| Пароль | `admin` |

**Смените пароль после первого входа.**

## Проверка работоспособности

```bash
# Сервер запущен — должен ответить статус 200
curl -I http://localhost:8080/
```

В логах должна появиться строка `Started Jetty Server` и успешное подключение к БД.

## Следующие шаги

- [Конфигурация](configuration.md) — настройка портов, БД, параметров приложения
- [Руководство по интеграции](integration-guide.md) — как подключить клиентские приложения

## See Also

- [Конфигурация](configuration.md) — все параметры настройки
- [Выпуск релиза](releasing.md) — сборка и публикация новых версий
