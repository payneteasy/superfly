drop procedure if exists ui_expire_invalid_sessions;
delimiter $$
create procedure ui_expire_invalid_sessions()
  begin
    update sessions
       set session_expired    = 'Y'
     where session_expired = 'N' and actions_expired = 'Y';

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_expire_invalid_sessions',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );