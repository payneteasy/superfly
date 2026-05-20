drop procedure if exists ui_suspend_user;
delimiter $$
create procedure ui_suspend_user(i_user_id int(11))
 main_sql:
  begin
    declare v_user_name       varchar(32);

  	update users set is_account_locked = 'Y', is_account_suspended = 'Y'
  		where user_id = i_user_id;

    select user_name into v_user_name
          from users where user_id=i_user_id;

    call int_create_event('ACCOUNT_SUSPEND', v_user_name);

  	select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_suspend_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );