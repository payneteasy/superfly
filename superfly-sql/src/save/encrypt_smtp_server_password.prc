drop procedure if exists encrypt_smtp_server_password;
delimiter $$
create procedure encrypt_smtp_server_password(i_ssrv_id int(10), i_password varchar(255))
 main_sql:
  begin
    -- a password that is already encrypted (e.g. changed in the UI while the startup task runs) is never overwritten
    update smtp_servers
       set password = i_password
     where ssrv_id = i_ssrv_id
       and password not like 'v2:%';
  end
$$
delimiter ;
