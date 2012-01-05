drop procedure if exists ui_delete_smtp_server;
delimiter $$
create procedure ui_delete_smtp_server(i_ssrv_id int(10))
 main_sql:
  begin
    declare v_usages text default null;

    select group_concat(ssys_id) from subsystems where ssrv_ssrv_id = i_ssrv_id into v_usages;

    if v_usages is null then
        delete from smtp_servers where ssrv_id = i_ssrv_id;
        select 'OK' status, null error_message;
    else
        select 'Used' status, concat('SMTP server is used by the following subsystems: ', v_usages) error_message;
    end if;
  end
$$
delimiter ;
call save_routine_information('ui_delete_smtp_server',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );