drop procedure if exists get_otp_pending_master_key_by_user_name;
delimiter $$
create procedure get_otp_pending_master_key_by_user_name(i_user_name varchar(32)
)
 main_sql:
  begin

  select otp_pending_master_key as totp_key
     from users where user_name = i_user_name;

  end
;
$$
delimiter ;
call save_routine_information('get_otp_pending_master_key_by_user_name',
                              concat_ws(',',
                                        'totp_key varchar'
                              )
     );
