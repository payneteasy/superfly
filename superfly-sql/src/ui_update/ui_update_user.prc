drop procedure if exists ui_update_user;
delimiter $$
create procedure ui_update_user(i_user_id int(10),
                                i_user_password varchar(32),
                                i_is_account_locked varchar(32),
                                i_logins_failed int(10),
                                i_last_login_date datetime
)
 main_sql:
  begin
    update users
       set user_password     = coalesce(i_user_password, user_password),
           is_account_locked   =
             coalesce(i_is_account_locked, is_account_locked),
           logins_failed     = coalesce(i_logins_failed, logins_failed),
           last_login_date   = coalesce(i_last_login_date, last_login_date)
     where user_id = i_user_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_update_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );