drop procedure if exists increment_hotp_logins_failed;
delimiter $$
create procedure increment_hotp_logins_failed(i_user_name varchar(32))
main_sql:
  begin
    update users set hotp_logins_failed = hotp_logins_failed + 1 where user_name = i_user_name;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('increment_hotp_logins_failed', concat_ws(',', 'status varchar', 'error_message varchar'));
