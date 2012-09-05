drop procedure if exists get_user_statuses;
delimiter $$
create procedure get_user_statuses(i_user_logins    text
)
 main_sql:
  begin

  select
    user_id,
    user_name,
    is_account_locked,
    last_login_date,
    logins_failed,
    (select access_date from unauthorised_access where printed_user_name = user_name order by access_date desc limit 1) last_failed_login_date,
    (select ip_address from unauthorised_access where printed_user_name = user_name order by access_date desc limit 1) last_failed_login_ip
  from
    users
  where
    i_user_logins is null or i_user_logins = '' or
      instr(concat(',', i_user_logins, ','), concat(',', user_name, ',')) > 0
  order by user_id;

  end
;
$$
delimiter ;
call save_routine_information('get_user_statuses',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'is_account_locked varchar',
                                        'last_login_date datetime',
                                        'logins_failed int',
                                        'last_failed_login_date datetime',
                                        'last_failed_login_ip varchar'
                              )
     );