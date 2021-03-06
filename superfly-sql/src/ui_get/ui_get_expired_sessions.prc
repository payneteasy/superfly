drop procedure if exists ui_get_expired_sessions;
delimiter $$
create procedure ui_get_expired_sessions()
 main_sql:
  begin
    select s.sess_id, u.user_id, u.user_name, s.callback_information, ss.send_callbacks
      from sessions s, users u, subsystems ss
     where     s.user_user_id = u.user_id and s.ssys_ssys_id = ss.ssys_id
           and s.session_expired = 'Y';
  end
$$
delimiter ;
call save_routine_information('ui_get_expired_sessions',
                              concat_ws(',',
                                        'sess_id int',
                                        'user_id int',
                                        'user_name varchar',
                                        'callback_information varchar',
                                        'send_callbacks varchar'
                              )
     );