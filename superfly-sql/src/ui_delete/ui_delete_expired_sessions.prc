drop procedure if exists ui_delete_expired_sessions;
delimiter $$
create procedure ui_delete_expired_sessions(i_expire_date datetime)
 main_sql:
  begin
    create temporary table if not exists temp_expired_sessions(
      sess_sess_id         int(10),
      user_user_id         int(10),
      user_name            varchar(100),
      callback_information varchar(100)
    )
    engine = memory;

    truncate table temp_expired_sessions;

    insert into temp_expired_sessions
          (
             sess_sess_id, user_user_id, user_name, callback_information
          )
      select s.sess_id, u.user_id, u.user_name, s.callback_information
        from sessions s, users u
       where s.user_user_id = u.user_id
             and(s.session_expired = 'Y'
                 or s.start_date <
                     coalesce(i_expire_date, str_to_date("10000101", "%Y%m%d")));

    delete s
      from sessions s,
           temp_expired_sessions ts
     where s.sess_id = ts.sess_sess_id;

    select sess_sess_id sess_id,
           user_user_id user_id,
           user_name,
           callback_information
      from temp_expired_sessions;
  end
$$
delimiter ;
call save_routine_information('ui_delete_expired_sessions',
                              concat_ws(',',
                                        'sess_id int',
                                        'user_id int',
                                        'user_name varchar',
                                        'callback_information varchar'
                              )
     );