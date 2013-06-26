drop procedure if exists ui_get_smtp_servers_list;
delimiter $$
create procedure ui_get_smtp_servers_list()
 main_sql:
  begin
    select ssrv_id,
           server_name,
           host,
           port,
           username,
           password,
           from_address,
           is_ssl
      from smtp_servers;
  end
$$
delimiter ;
call save_routine_information('ui_get_smtp_servers_list',
                              concat_ws(',',
                                        'ssrv_id int',
                                        'server_name varchar',
                                        'host varchar',
                                        'port int',
                                        'username varchar',
                                        'password varchar',
                                        'from_address varchar',
                                        'is_ssl varchar'
                              )
     );