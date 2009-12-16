@echo off
@mkdir target
@echo Installing MySQL procedures::
@mysql -h127.0.0.1 -P3306 -b -vv -u sso -p123sso123 sso --comments --show-warnings < all-proc.sql > target\all-proc.log
@echo Current warnings:
@find /N " warning" target\all-proc.log 
@find /N "Note" target\all-proc.log