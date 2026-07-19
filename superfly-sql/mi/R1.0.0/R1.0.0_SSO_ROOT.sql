drop database if exists sso;
create database sso default character set utf8 collate utf8_general_ci;
GRANT ALL PRIVILEGES ON sso.* TO 'sso'@'%' IDENTIFIED BY '123sso123';
FLUSH PRIVILEGES;
