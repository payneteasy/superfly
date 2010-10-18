@echo off
@cd ../../src
@call all-proc.bat
@cd ../mi/R1.2.0
@mkdir target
@echo Current DDL errors:
@mysql -b -vv -u sso -p123sso123 sso --comments --show-warnings < R1.2.0_SSO.sql > target\R1.2.0_SSO.log
@echo Current DDL warnings:
@find /N " warning" target\R1.2.0_SSO.log 
@find /N "Note" target\R1.2.0_SSO.log
@echo Current DML errors:
@mysql -b -vv -u sso -p123sso123 sso --comments --show-warnings < R1.2.0_SSO_DML.sql > target\R1.2.0_SSO_DML.log
@echo Current DML warnings:
@find /N " warning" target\R1.2.0_SSO_DML.log 
@find /N "Note" target\R1.2.0_SSO_DML.log
@pause