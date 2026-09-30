# Проверка SQL миграций (mi)

## Внесённые исправления

### 1. `src/run_install_command.sql`
- **Проблема:** Двойной вызов `DEALLOCATE PREPARE v_stmt` — при срабатывании условия выполнялся дважды, что вызывало ошибку.
- **Исправление:** `DEALLOCATE PREPARE` вызывается один раз сразу после `EXECUTE`, до блока `IF`.

### 2. `mi/R1.7.0/quartz-to-2.0.sql`
- **Проблема:** Неявная область видимости для `SET foreign_key_checks`.
- **Исправление:** Используется `SET SESSION foreign_key_checks` для явного указания сессии.

## Проверка синтаксиса MySQL/MariaDB

| Элемент | Статус |
|---------|--------|
| `SET SESSION foreign_key_checks` | ✓ Корректно (MySQL 5.7+, MariaDB) |
| `CREATE TABLE ... ENGINE=InnoDB DEFAULT CHARSET=utf8` | ✓ Корректно |
| `INSERT ... SELECT` с `CASE WHEN` | ✓ Корректно |
| `RENAME TABLE` | ✓ Корректно |
| `DROP TABLE` / `DROP TABLE IF EXISTS` | ✓ Корректно |
| `FOREIGN KEY` в `CREATE TABLE` | ✓ Корректно |
| `NUMERIC(13,4)` | ✓ Корректно (синоним DECIMAL) |
| `TINYINT(1)` для bool | ✓ Корректно |

## Замечания

### R1.0.0_SSO_ROOT.sql
- `GRANT ... IDENTIFIED BY` — устарел в MySQL 8.0.16+, но поддерживается в MariaDB.
- Для MySQL 8.0+ рекомендуется: `CREATE USER` + `GRANT` отдельно.

### quartz-to-2.0.sql
- При запуске `all_mi.sh` база пересоздаётся в R1.0.0 (`drop database if exists sso`), поэтому миграция всегда выполняется с чистого состояния.
- Скрипт рассчитан на однократное выполнение в рамках полного прогона миграций.

### run_install_command.sql
- `delimiter $$` — команда mysql-клиента, при `mysql < file.sql` обрабатывается корректно.
- SQLSTATE коды (`42S21`, `42S02` и т.д.) — специфичны для MySQL/MariaDB.
