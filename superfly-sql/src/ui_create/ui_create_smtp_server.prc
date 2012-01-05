drop procedure if exists ui_create_smtp_server;
delimiter $$
create procedure ui_create_smtp_server(i_server_name varchar(32),
                                       i_host varchar(64),
                                       i_port int(10),
                                       i_username varchar(64),
                                       i_password varchar(64),
                                       i_from_address varchar(64),
                                       out o_ssrv_id int(10)
)
 main_sql:
  begin
    insert into smtp_servers
          (
             server_name, host, port, username, password, from_address
          )
    values (i_server_name, i_host, i_port, i_username, i_password, i_from_address);

    set o_ssrv_id   = last_insert_id();

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_smtp_server',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );