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

1. Миграция `superfly-sql/mi/R1.7.8` — колонка `users.otp_pending_master_key`.
2. Миграция `superfly-sql/mi/R1.7.9` — `admin` с паролем по умолчанию получает временный пароль (смена при первом входе),
   опубликованный `hotp_salt` `admin` заменяется случайным.
3. Переустановка процедур (`superfly-sql/src/all-proc.sh`): новые `get_otp_pending_master_key_by_user_name`,
   `save_otp_pending_master_key`, `confirm_otp_pending_master_key`; изменена `get_user_password_history_and_current_password`
   (текущий пароль, в том числе временный, затем собственные пароли пользователя от новых к старым); изменена `save_google_auth_master_key` (теперь также очищает
   `otp_pending_master_key`).

**Изоляция входа.** `authenticate`, `pseudoAuthenticate`, remote-auth `check-password` и SSO-форма логина пускают только
пользователей с ролью в подсистеме входа и без роли в `superfly`. Учётка, у которой есть роли и в админке, и в подсистеме,
в подсистему больше не войдёт — заведите отдельные учётки. Проверить до выкатки: пользователи с ролями в `superfly` и других подсистемах.

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
