drop procedure if exists clear_hotp_logins_failed;
delimiter $$
create procedure clear_hotp_logins_failed(i_user_name varchar(32))
main_sql:
  begin
    update users set hotp_logins_failed = 0 where user_name = i_user_name;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('clear_hotp_logins_failed', concat_ws(',', 'status varchar', 'error_message varchar'));
