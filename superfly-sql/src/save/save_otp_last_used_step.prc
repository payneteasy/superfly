drop procedure if exists save_otp_last_used_step;
delimiter $$
create procedure save_otp_last_used_step(i_user_name varchar(32), i_step bigint)
 main_sql:
  begin
    -- compare-and-set: of two concurrent requests with the same code only one updates the row
    update users set otp_last_used_step = i_step
     where user_name = i_user_name and (otp_last_used_step is null or otp_last_used_step < i_step);
    select row_count() as updated_count;
  end
$$
delimiter ;
call save_routine_information('save_otp_last_used_step', concat_ws(',', 'updated_count int'));
