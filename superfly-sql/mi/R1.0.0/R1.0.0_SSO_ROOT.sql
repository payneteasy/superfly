drop database if exists sso;
create database sso default character set utf8 collate utf8_general_ci;
grant all privileges on sso.* to 'sso'@'localhost' identified by '123sso123' with grant option;
