drop procedure if exists get_smtp_servers_with_plain_password;
delimiter $$
create procedure get_smtp_servers_with_plain_password()
 main_sql:
  begin
    -- passwords not yet in the CryptoService "v2:" format; used by the startup encryption task
    select ss.ssrv_id,
           ss.server_name,
           ss.password
      from smtp_servers ss
     where ss.password is not null
       and ss.password <> ''
       and ss.password not like 'v2:%';
  end
$$
delimiter ;
call save_routine_information('get_smtp_servers_with_plain_password',
                              concat_ws(',',
                                        'ssrv_id int',
                                        'server_name varchar',
                                        'password varchar'
                              )
     );
