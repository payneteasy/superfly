# Инструкция по релизу проекта Superfly

## Настройка окружения

1. Убедитесь, что у вас настроен GPG ключ:
   ```bash
   gpg --list-keys
   ```
   Если ключа нет, создайте его:
   ```bash
   gpg --gen-key
   ```

2. Отправьте ваш публичный ключ на сервер ключей:
   ```bash
   gpg --keyserver hkp://keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. Настройте Maven с вашими учетными данными. В файле `~/.m2/settings.xml` добавьте:
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

### Автоматический способ (рекомендуется)

Используйте скрипт `release.sh`:

```bash
chmod +x release.sh
./release.sh ваш_пароль_gpg
```

### Ручной способ

1. Подготовка релиза:
   ```bash
   mvn --batch-mode release:prepare
   ```

2. Выполнение релиза:
   ```bash
   mvn release:perform -Darguments=-Dgpg.passphrase=ваш_пароль_gpg
   ```

## Проверка результатов

После успешного выполнения релиза ваши артефакты будут автоматически опубликованы в Maven Central (если настроен `autoPublish=true`). Если автоматическая публикация отключена, перейдите на портал Central Portal для завершения процесса публикации.

## Решение проблем

Если возникают ошибки:

1. Убедитесь, что ваши GPG ключи правильно настроены и отправлены на сервер.
2. Проверьте корректность настройки `settings.xml`.
3. Убедитесь, что все зависимости проекта доступны.

Для более подробной информации о процессе публикации посетите [официальную документацию Sonatype](https://central.sonatype.org/publish/publish-portal-maven/).
