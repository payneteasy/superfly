#!/bin/bash

# Скрипт для автоматизации релиза в Maven Central

# Проверяем, что gpg passphrase передан как аргумент
if [ $# -lt 1 ]; then
    echo "Использование: $0 <gpg-passphrase>"
    exit 1
fi

GPG_PASSPHRASE=$1

# Подготовка релиза
echo "Запуск подготовки релиза..."
mvn --batch-mode release:prepare

if [ $? -ne 0 ]; then
    echo "Ошибка при подготовке релиза!"
    exit 1
fi

# Выполнение релиза
echo "Выполнение релиза..."
mvn release:perform -Darguments="-Dgpg.passphrase=${GPG_PASSPHRASE}"

if [ $? -ne 0 ]; then
    echo "Ошибка при выполнении релиза!"
    exit 1
fi

echo "Релиз успешно выполнен!"
