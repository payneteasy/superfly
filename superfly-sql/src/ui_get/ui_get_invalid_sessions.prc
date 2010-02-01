drop procedure if exists ui_get_invalid_sessions;
delimiter $$
create procedure ui_get_invalid_sessions()
 main_sql:
  begin
    select s.sess_id, u.user_id, u.user_name, s.callback_information
      from sessions s, users u
     where     s.user_user_id = u.user_id
           and s.session_expired = 'N'
           and s.actions_expired = 'Y';
  end
$$
delimiter ;
call save_routine_information('ui_get_invalid_sessions',
                              concat_ws(',',
                                        'sess_id int',
                                        'user_id int',
                                        'user_name varchar',
                                        'callback_information varchar'
                              )
     );