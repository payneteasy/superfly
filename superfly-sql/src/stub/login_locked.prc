drop procedure if exists login_locked;
delimiter $$
create procedure login_locked(i_user_name varchar(32), i_max_logins_failed int(10))
main_sql:
  begin
    declare v_user_id int(10);
    declare v_logins_failed int(10);
    declare v_is_account_locked varchar(1);
    set v_user_id = null;
    set v_logins_failed = null;
    set v_is_account_locked = 'N';
    
     select user_id, is_account_locked, logins_failed into v_user_id, v_is_account_locked, v_logins_failed from users u where u.user_name = i_user_name;
      if v_user_id is not null and v_is_account_locked <> 'Y' and coalesce(v_logins_failed,0) >= i_max_logins_failed
            then call ui_lock_user(v_user_id);
      end if;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('login_locked', concat_ws(',', 'status varchar', 'error_message varchar'));
