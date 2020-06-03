drop procedure if exists update_user_otp_type;
delimiter $$
create procedure update_user_otp_type(i_user_name varchar(32),
                                      i_otp_code varchar(16))
 main_sql:
  begin
   declare v_otp_otp_type_id   int(10);

    select otp_type_id into v_otp_otp_type_id
      from otp_types where otp_code=i_otp_code;


	update users set otp_otp_type_id=v_otp_otp_type_id where user_name=i_user_name;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('update_user_otp_type',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );