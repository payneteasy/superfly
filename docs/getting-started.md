[Back to README](../README.md) · [Конфигурация →](configuration.md)

# Установка и запуск

## Требования

| Компонент | Версия |
|-----------|--------|
| Java | 21+ |
| Maven | 3.9+ (или `./mvnw`) |
| MySQL | **5.7** |
| Docker | любая (для базы и образа) |
| Git | любая |

> **Только MySQL 5.7.** На 8.0 схема не устанавливается: `groups` там стало зарезервированным словом.

---

## Быстрый старт (локальная разработка)

```bash
git clone https://github.com/payneteasy/superfly.git
cd superfly
./mvnw -DskipTests package

./dev-env.sh up      # MySQL 5.7, схема и хранимые процедуры
./dev-env.sh app     # приложение на http://localhost:8085/superfly/
```

Откройте `http://localhost:8085/superfly/`, логин `admin`, пароль `123admin123`.
Пароль временный: первый вход ведёт на страницу смены пароля, остальная админка доступна после смены.
Пока у `admin` дефолтный пароль, приложение пишет при старте ERROR в лог.

`dev-env.sh` публикует MySQL только на `127.0.0.1:3344`. Остальные команды (`seed`, `sql`, `down`) и ограничения
описаны в [README](../README.md#локальная-разработка). `./dev-env.sh app` сам задаёт `DB_*` и `JETTY_*` (база `127.0.0.1:3344`) и запускает `StartSuperfly` со встроенным Jetty.
Собранный JAR запускается так (параметры — [переменные окружения](configuration.md#база-данных)):

```bash
DB_HOST=127.0.0.1 DB_PORT=3344 DB_PASSWORD=... SUPERFLY_CRYPTO_SECRET=... SUPERFLY_CRYPTO_SALT=... \
  java -jar superfly-web/target/superfly.jar
```

WAR больше не собирается: артефакт — `superfly-web/target/superfly.jar` (shaded, ~70 МБ).

### Интеграционные тесты

DAO-тесты (`superfly-integration-test`) ходят в реальную MySQL 5.7 и по умолчанию пропускаются. Отдельная база
поднимается тем же `dev-env.sh` с другими именем контейнера, сети и портом, чтобы не задеть рабочее окружение:

```bash
export SUPERFLY_DEV_CONTAINER=superfly-test-db SUPERFLY_DEV_NETWORK=superfly-test-net SUPERFLY_DEV_PORT=3401
SSO_DB_DATABASE=ssotest ./dev-env.sh up
./mvnw -B -pl superfly-integration-test -am -Pintegration-test verify \
  -Dsso.db.url='jdbc:mysql://127.0.0.1:3401/ssotest?autoReconnect=true&characterEncoding=utf8&serverTimezone=Europe/Moscow' \
  -Dsso.db.skipCreate=true
./dev-env.sh down
```

Без `-Dsso.db.skipCreate=true` тесты сами запускают `src/test/sh/create_test_database.sh`: он пересоздаёт базу
`ssotest` (не `sso`) и накатывает миграции и процедуры; для этого нужны `mysql` в `PATH` и переменные
`SSO_DB_HOST`, `SSO_DB_PORT`, `SSO_DB_ROOT_PASSWORD` (по умолчанию `localhost`, `3344`, `1234`).
Адрес, пользователь и пароль тестов — `-Dsso.db.url`, `-Dsso.db.user`, `-Dsso.db.password`
(по умолчанию `127.0.0.1:3344/ssotest`, `sso`/`123sso123`).

---

## Обновление существующей базы

Миграции лежат в `superfly-sql/mi/R<version>/`. Скрипты читают переменные окружения:

| Переменная | По умолчанию | Описание |
|------------|-------------|---------|
| `SSO_DB_HOST` | `localhost` | Хост БД |
| `SSO_DB_PORT` | `3344` | Порт |
| `SSO_DB_ROOT` | `root` | Root-пользователь |
| `SSO_DB_ROOT_PASSWORD` | dev-значение | Root-пароль (для не-dev базы задайте свой) |
| `SSO_DB_USERNAME` | `sso` | Пользователь приложения |
| `SSO_DB_PASSWORD` | dev-значение | Пароль приложения (для не-dev базы задайте свой) |
| `SSO_DB_DATABASE` | `sso` | Имя базы |

```bash
cd superfly-sql/mi
version_from=R1.7.4 bash all_mi.sh     # с какой версии применять; по умолчанию R1.0.0
cd ../src && ./all-proc.sh             # хранимые процедуры ставятся отдельно от приложения
```

Миграции прерываются на первой ошибке. Что делать при обновлении с конкретных версий — в
[заметках об обновлении](migration-client-ee8-ee10.md#обновление). Версию задеплоенного приложения отдаёт
`/management/version.txt` без авторизации.

---

## Docker-образ

Образ собирается из `Dockerfile` (стадия `production`): `superfly.jar` со встроенным Jetty 12 (ee10) на JRE 21, непривилегированный пользователь `superfly`.
Схему БД образ не ставит — примените миграции и процедуры, как описано выше.

```bash
docker build --target production -t superfly-app .

docker run -d -p 8080:8080 \
  -e DB_HOST=db.example.com -e DB_PORT=3306 -e DB_NAME=sso \
  -e DB_USER=sso -e DB_PASSWORD -e DB_TIMEZONE=UTC \
  -e SUPERFLY_CRYPTO_SECRET -e SUPERFLY_CRYPTO_SALT \
  superfly-app
```

Параметры `DB_*` и `JETTY_*` читаются приложением из окружения (порт — `JETTY_PORT`, по умолчанию 8080). Если контекст не стартовал (например, БД недоступна), процесс завершается с кодом 1, а не отвечает 503.
`SUPERFLY_CRYPTO_SECRET` и `SUPERFLY_CRYPTO_SALT` обязательны (ключ шифрования OTP, см. [Конфигурацию](configuration.md#ключ-шифрования-otp-master-key)): без них приложение не стартует.
`-e DB_PASSWORD` без значения берёт пароль из окружения хоста, чтобы он не попадал в историю команд.

`compose.yml` поднимает приложение и MySQL 5.7 (`.env` создаётся из `.env.example`; `DB_PASSWORD` и
`DB_ROOT_PASSWORD` обязательны). `compose.production.yml` добавляет hardening (`read_only`, `cap_drop`, лимиты ресурсов)
и требует явно задать `JETTY_TRUST_FORWARDED` (`true` за прокси, см. [Reverse proxy](configuration.md#reverse-proxy-и-cookie)):

```bash
docker compose -f compose.yml -f compose.production.yml up -d
```

`compose.override.yml` подхватывается автоматически и публикует MySQL на `127.0.0.1`.

---

## Устранение неисправностей

| Симптом | Причина | Решение |
|---------|---------|---------|
| `Connection refused` на 3344 | База не запущена | `./dev-env.sh up` |
| `Table 'sso.users' doesn't exist` | Миграции не накатаны | `./dev-env.sh up` или `all_mi.sh` |
| Схема не ставится, ошибка на `groups` | MySQL 8.0 | Использовать MySQL 5.7 |
| Процесс завершился при старте | Нет подключения к БД | Проверить `DB_*` (хост, порт, пароль) |

---

## Следующие шаги

- [Конфигурация](configuration.md) — база данных, политики, CSP
- [Руководство по интеграции](integration-guide.md) — подключение клиентских приложений
- [Миграция EE8/EE10](migration-client-ee8-ee10.md) — javax/jakarta split, breaking changes
