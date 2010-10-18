drop procedure if exists ui_unlock_user;
delimiter $$
create procedure ui_unlock_user(i_user_id int(10))
 main_sql:
  begin
    update users
       set is_account_locked    = "N", logins_failed=null
     where user_id = i_user_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_unlock_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );