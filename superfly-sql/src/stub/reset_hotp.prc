drop procedure if exists reset_hotp;
delimiter $$
create procedure reset_hotp(i_user_name varchar(32), i_hotp_salt varchar(64))
main_sql:
  begin
    update users set hotp_logins_failed = 0, hotp_counter = 0, hotp_salt = i_hotp_salt where user_name = i_user_name;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('reset_hotp', concat_ws(',', 'status varchar', 'error_message varchar'));
