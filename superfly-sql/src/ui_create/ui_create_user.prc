drop procedure if exists ui_create_user;
delimiter $$
create procedure ui_create_user(i_user_name varchar(32),
                                i_user_password varchar(128),
                                i_user_email varchar(255),
                                i_role_id int(10),
                                i_name varchar(32),
                                i_surname varchar(32),
                                i_secret_question varchar(255),
                                i_secret_answer varchar(255),  
                                i_salt varchar(64),
                                i_hotp_salt varchar(64),
                                i_is_password_temp varchar(1),
                                i_public_key text,
                                i_user_organization varchar(255),
                                i_is_otp_optional varchar(1),
                                out o_user_id int(10)
)
 main_sql:
  begin
    select user_id from users where user_name = i_user_name into o_user_id;
    
    if o_user_id is not null then
    	select 'duplicate' status, 'User already exists' error_message;
    	leave main_sql;
    end if;

    insert into users
          (
             user_name, user_password, email, is_account_locked, name, 
             surname, secret_question, secret_answer, salt, hotp_salt, is_password_temp, create_date,
             public_key, completed , user_organization, is_otp_optional
          )
    values (i_user_name, i_user_password, i_user_email,'N', 
            i_name, 
            i_surname, 
            i_secret_question, 
            i_secret_answer, 
            i_salt, 
            i_hotp_salt, 
            i_is_password_temp , now(),
            i_public_key, 'Y', i_user_organization, i_is_otp_optional);

    set o_user_id   = last_insert_id();


    insert into user_history (user_user_id, user_password, salt, number_history, start_date, end_date)
         values (o_user_id, i_user_password, i_salt, 1, now(), '2999-12-31');


    insert into user_roles
          (
            user_user_id, role_role_id
          )
    values (o_user_id, i_role_id);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );