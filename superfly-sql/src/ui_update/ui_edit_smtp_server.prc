drop procedure if exists ui_edit_smtp_server;
delimiter $$
create procedure ui_edit_smtp_server(i_ssrv_id int(10),
                                     i_server_name varchar(32),
                                     i_host varchar(64),
                                     i_port int(10),
                                     i_username varchar(64),
                                     i_password varchar(64),
                                     i_from_address varchar(64)
)
 main_sql:
  begin
    update smtp_servers
       set server_name = i_server_name,
           host = i_host,
           port = i_port,
           username = i_username,
           password = i_password,
           from_address = i_from_address
     where ssrv_id = i_ssrv_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_edit_smtp_server',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );