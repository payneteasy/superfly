drop procedure if exists ui_lock_user;
delimiter $$
create procedure ui_lock_user(i_user_id int(10))
 main_sql:
  begin
    declare v_user_name       varchar(32);

    update users
       set is_account_locked    = "Y"
     where user_id = i_user_id;

    select user_name into v_user_name
          from users where user_id=i_user_id;

    call int_create_event('ACCOUNT_LOCK', v_user_name);
     
/*
    update sessions
       set session_expired    = 'Y'
     where user_user_id = i_user_id;
*/

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_lock_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );