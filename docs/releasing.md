[← Миграция EE8 / EE10](migration-client-ee8-ee10.md) · [Back to README](../README.md)

# Выпуск релиза

Инструкция по публикации новой версии Superfly в Maven Central.

## Требования

- GPG-ключ для подписи артефактов
- Учётная запись на [Sonatype Central Portal](https://central.sonatype.com/)
- Maven-профиль `gpg-sign` в `~/.m2/settings.xml`

## Настройка окружения

### 1. Создать / проверить GPG-ключ

```bash
gpg --list-keys
# Если ключа нет:
gpg --gen-key
```

### 2. Отправить ключ на keyserver

```bash
gpg --keyserver hkp://keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Настроить `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>ваш_логин_sonatype</username>
      <password>ваш_токен_sonatype</password>
    </server>
  </servers>
</settings>
```

## Выполнение релиза

### Автоматически (рекомендуется)

```bash
chmod +x release.sh
./release.sh ваш_пароль_gpg
```

Скрипт `release.sh` выполняет подготовку и деплой в один шаг.

### Вручную

```bash
# 1. Подготовить релиз (обновляет версию, создаёт тег)
./mvnw --batch-mode release:prepare

# 2. Выполнить релиз (подписывает и публикует артефакты)
./mvnw release:perform -P gpg-sign -Dgpg.passphrase=ваш_пароль_gpg
```

## Проверка результатов

После успешного деплоя артефакты публикуются в Maven Central автоматически
(если `autoPublish=true`). Иначе — перейдите на [Central Portal](https://central.sonatype.com/)
и завершите публикацию вручную.

## Заметки к релизу 2.0

**Артефакт.** WAR больше нет: `superfly-web` собирает исполняемый `superfly-web/target/superfly.jar` (shaded, ~70 МБ) со встроенным
Jetty; запуск — `java -jar` с переменными окружения (см. [Конфигурацию](configuration.md#база-данных)). `release:perform`
публикует этот shaded-артефакт `superfly-web` в Maven Central.

**База данных.** Перед выкаткой приложения:

1. Миграция `superfly-sql/mi/R2.0.0` (все шаги идемпотентны):
   - `users.master_key` расширяется до `varchar(128)` (шифротекст `v2:` занимает 83 символа);
   - `users.hotp_logins_failed` со значением `NULL` обнуляется;
   - `users.is_otp_optional` по умолчанию `'N'` (существующие записи не меняются);
   - новые колонки `users.otp_last_used_step` (шаг времени последнего принятого TOTP-кода) и `users.otp_pending_master_key`;
   - `admin` с паролем по умолчанию получает временный пароль (смена при первом входе), опубликованный `hotp_salt` `admin`
     заменяется случайным;
   - `subsystems.subsystem_token` хранится как `sha256:<hex>`; существующие токены хэшируются, подсистемы продолжают
     работать со своими токенами. Показать текущий токен админка больше не может — только сгенерировать новый (он
     показывается один раз);
   - из `user_history` удаляются хэши старого формата за пределами окна проверки повторного использования (5 последних
     записей пользователя; необратимо, на проверку не влияет);
   - `smtp_servers.password` расширяется до `varchar(255)` под шифротекст.

   Обновление с 1.7: `version_from=R2.0.0 bash all_mi.sh`, затем `all-proc.sh`. На стендах, где уже применялись
   промежуточные миграции 2.0, тоже достаточно `version_from=R2.0.0` — все шаги идемпотентны.
2. Переустановка процедур (`superfly-sql/src/all-proc.sh`): новые `get_otp_pending_master_key_by_user_name`,
   `save_otp_pending_master_key`, `confirm_otp_pending_master_key`; изменена `get_user_password_history_and_current_password`
   (текущий пароль, в том числе временный, затем собственные пароли пользователя от новых к старым); изменена `save_google_auth_master_key` (теперь также очищает
   `otp_pending_master_key`); новые `get_subsystem_auth`, `get_subsystem_private_key`, `get_subsystems_with_plain_private_key`,
   `encrypt_subsystem_private_key`; `ui_get_subsystem*` больше не отдают токен и приватный ключ; новые
   `get_smtp_servers_with_plain_password`, `encrypt_smtp_server_password`; изменены `ui_get_users_list` (без хэша пароля),
   `ui_create_subsystem`/`ui_edit_subsystem_properties` (подсистема без SMTP-сервера), `ui_clone_user`,
   `get_subsystem_private_key`, `int_check_user_password` (старые хэши в истории заменяются при входе),
   SMTP-процедуры `ui_create_smtp_server`/`ui_edit_smtp_server`/`ui_get_smtp_servers_list` и процедуры привязки
   ролей/групп/действий (`int_link_*`, `int_unlink_*`, `ui_change_actions_log_level`: проверка списков id).
   Новое приложение со старыми процедурами не стартует.
5. Первый старт приложения шифрует приватные ключи подсистем и пароли SMTP-серверов (`SUPERFLY_CRYPTO_SECRET`, формат `v2:`);
   до него remote-auth принимает ключи в открытом виде с WARN в логе. Откат на 1.7 после этого ломает отправку почты.

**Подписанные уведомления.** Сервер подписывает уведомления подсистемам хэшем их токена и не шлёт их подсистемам без токена.
Клиент 2.0 принимает только подписанные уведомления и только с настроенным `notificationSecret`: обновляйте клиент
(paynet) одновременно с сервером и пропишите токен, иначе logout-уведомления будут отклоняться с `403`.

**Вход через SSO.** Клиент 2.0 обменивает токен только вместе с параметром `state`, который сам положил в сессию перед
редиректом на вход. Spring Security-приложениям нужно заменить entry point на `SuperflySSOLoginUrlAuthenticationEntryPoint`
(см. [Redirect-based SSO](integration-guide.md#redirect-based-sso)), иначе SSO-вход перестанет работать.

**Клиентские фильтры.** Исключённые пути (`ExcludedPaths`) сравниваются по целым сегментам: `/static` больше не покрывает
`/staticX` и `/static.css` — перепишите исключения-префиксы на каталоги. После входа меняется session id; `targetUrl`
после обмена токена принимается только как локальный путь.

**Изоляция входа.** `authenticate`, `pseudoAuthenticate`, remote-auth `check-password` и SSO-форма логина пускают только
пользователей с ролью в подсистеме входа. Учётка с ролями и в `superfly`, и в подсистеме входит в подсистему и читается
ею, но подсистема через API не может менять ей пароль, OTP, данные и роли — это делается в админке
(см. [Subsystem isolation](integration-guide.md#subsystem-isolation-20)).

**Ограничения и журнал.** Тела запросов: `/remoting/*` — до 1 МБ, `/sso/check/*` — до 64 КБ, больше — `413`.
`getEvents`: одновременно ждут не больше 4 запросов подсистемы и 50 всего, остальные получают пустой список после паузы ~1 с.
Имя пользователя длиннее 32 символов обрабатывается как неизвестный пользователь. `getUserDescription` больше не отдаёт
`secretAnswer`. Отказы по правилу доступа подсистемы пишутся в журнал событий (`SUBSYSTEM_ACCESS_DENIED`, уровень ERROR,
как прочие отказы). Выход из админки — только POST. Письмо с новым паролем шифруется PGP AES-256 (было CAST5).

**API.** `SSOService.checkOtp` возвращает `CheckOtpResult` вместо `boolean`, HTTP-тело — `{"status":"..."}`; добавлен
`confirmOtpMasterKey`; `resetGoogleAuthMasterKey` теперь сохраняет pending-ключ (см. [API](api.md#checkotp)).
Клиентов и сервер обновляйте вместе.

**SPI.** В `HOTPService` (`superfly-spi-support`) добавлен метод `confirmGoogleAuthMasterKey`, а `validateGoogleTimePassword`
возвращает `CheckOtpResult.Status`: сторонние реализации не скомпилируются без доработки.

## Устранение неполадок

| Проблема | Решение |
|----------|---------|
| GPG-ключ не найден | Убедитесь, что ключ создан и экспортирован на keyserver |
| Ошибка аутентификации Sonatype | Проверьте `settings.xml`, используйте токен (не пароль) |
| Зависимости недоступны | Проверьте VPN и доступ к репозиторию `maven.pne.io` |

## See Also

- [Установка и запуск](getting-started.md) — сборка из исходников
- [Конфигурация](configuration.md) — настройка окружения
