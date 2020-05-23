drop procedure if exists get_google_auth_master_key_by_user_name;
delimiter $$
create procedure get_google_auth_master_key_by_user_name(i_user_name varchar(32)
)
 main_sql:
  begin

  select master_key as totp_key
     from users where user_name = i_user_name;

  end
;
$$
delimiter ;
call save_routine_information('get_google_auth_master_key_by_user_name',
                              concat_ws(',',
                                        'totp_key varchar'
                              )
     );