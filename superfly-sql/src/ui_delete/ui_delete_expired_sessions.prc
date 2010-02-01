drop procedure if exists ui_delete_expired_sessions;
delimiter $$
create procedure ui_delete_expired_sessions(i_expire_date datetime)
 main_sql:
  begin
    delete from sessions
     where session_expired = 'Y'
           and start_date <
                coalesce(i_expire_date, str_to_date("10000101", "%Y%m%d"));

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_expired_sessions',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );