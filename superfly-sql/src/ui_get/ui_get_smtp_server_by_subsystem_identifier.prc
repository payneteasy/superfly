drop procedure if exists ui_get_smtp_server_by_subsystem_identifier;
delimiter $$
create procedure ui_get_smtp_server_by_subsystem_identifier(i_subsystem_name varchar(255))
 main_sql:
  begin
    select ssrv_id,
           server_name,
           host,
           port,
           username,
           password,
           from_address
      from smtp_servers, subsystems
      where ssrv_id = ssrv_ssrv_id and subsystem_name = i_subsystem_name;
  end
$$
delimiter ;
call save_routine_information('ui_get_smtp_server_by_subsystem_identifier',
                              concat_ws(',',
                                        'ssrv_id int',
                                        'server_name varchar',
                                        'host varchar',
                                        'port int',
                                        'username varchar',
                                        'password varchar',
                                        'from_address varchar'
                              )
     );