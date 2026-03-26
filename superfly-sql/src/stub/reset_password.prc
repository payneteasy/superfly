drop procedure if exists reset_password;
delimiter $$
create procedure reset_password(i_user_id int(10),
                                i_user_password varchar(128)
                               )
main_sql:
  begin
    declare v_user_name       varchar(32);

    select user_name into v_user_name
          from users where user_id=i_user_id;


    update users u 
      set 
           u.user_password = coalesce(i_user_password,user_password), 
           u.is_password_temp = 'Y',
	       u.is_account_locked = 'N',
           u.logins_failed = null,
           u.hotp_logins_failed = null
           where u.user_id = i_user_id;

    call int_create_event('PASSWORD_RESET', v_user_name);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('reset_password', concat_ws(',', 'status varchar', 'error_message varchar'));

