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

## Устранение неполадок

| Проблема | Решение |
|----------|---------|
| GPG-ключ не найден | Убедитесь, что ключ создан и экспортирован на keyserver |
| Ошибка аутентификации Sonatype | Проверьте `settings.xml`, используйте токен (не пароль) |
| Зависимости недоступны | Проверьте VPN и доступ к репозиторию `maven.pne.io` |

## See Also

- [Установка и запуск](getting-started.md) — сборка из исходников
- [Конфигурация](configuration.md) — настройка окружения
