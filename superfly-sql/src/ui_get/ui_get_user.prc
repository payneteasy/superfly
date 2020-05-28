drop procedure if exists ui_get_user;
delimiter $$
create procedure ui_get_user(i_user_id int(10))
 main_sql:
  begin
    select user_id
         , user_name
         , user_password
         , email as user_email
         , name
         , surname
         , secret_question
         , secret_answer 
         , salt
         , public_key
         , is_account_suspended
         , is_account_locked
         , user_organization
         , otp.otp_name
    from users u
      left join otp_types otp on u.otp_otp_type_id=otp.otp_type_id
      where user_id = i_user_id;
  end                                                    
$$
delimiter ;
call save_routine_information('ui_get_user',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'user_password varchar',
                                        'user_email varchar',
                                        'name varchar',
                                        'surname varchar',
                                        'secret_question varchar',
                                        'secret_answer varchar',
                                        'salt varchar',
                                        'public_key varchar',
                                        'is_account_suspended varchar',
                                        'is_account_locked varchar',
                                        'user_organization varchar',
                                        'otp_name varchar'
                              )
     );