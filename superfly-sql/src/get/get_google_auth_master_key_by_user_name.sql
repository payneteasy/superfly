drop procedure if exists get_google_auth_master_key_by_user_name;
delimiter $$
create procedure get_google_auth_master_key_by_user_name(i_user_name varchar(32)
)
 main_sql:
  begin

  select
    "jQMLv0BytIbLBWsvRyoOoPTgE3u5u+r7gPbnmX9h4lQ=" as totp_key; -- 3AMG2U2KDK22ED5P

  end
;
$$
delimiter ;
call save_routine_information('get_google_auth_master_key_by_user_name',
                              concat_ws(',',
                                        'totp_key varchar'
                              )
     );