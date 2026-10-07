drop procedure if exists confirm_otp_pending_master_key;
delimiter $$
create procedure confirm_otp_pending_master_key(i_user_name varchar(32), i_pending_secret_key varchar(128), i_step bigint)
 main_sql:
  begin
    -- compare-and-set: the key becomes active only if it is still the one the confirmation code was checked
    -- against, a concurrent reset replaces it and this update matches nothing; binary: the collation ignores case.
    -- The step of the confirmation code is used up by the same statement: no login with that code may see the new
    -- key before its step is stored.
    update users set master_key = i_pending_secret_key, otp_pending_master_key = null,
                     otp_last_used_step = greatest(coalesce(otp_last_used_step, i_step), i_step)
     where user_name = i_user_name and otp_pending_master_key = binary i_pending_secret_key;
    select row_count() as updated_count;
  end
$$
delimiter ;
call save_routine_information('confirm_otp_pending_master_key', concat_ws(',', 'updated_count int'));
