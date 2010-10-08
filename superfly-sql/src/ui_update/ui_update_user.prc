drop procedure if exists ui_update_user;
delimiter $$
create procedure ui_update_user(i_user_id int(10),
                                i_user_name varchar(32), -- dummy field for java...
                                i_user_password varchar(128),
                                i_user_email varchar(255),
                                i_name varchar(32),
                                i_surname varchar(32),
                                i_secret_question varchar(255),
                                i_secret_answer varchar(255),
                                i_salt varchar(64)
                                
)
 main_sql:
  begin
    update users
       set user_password     = coalesce(i_user_password, user_password),   
           salt = coalesce(i_salt, salt),    
           email  = i_user_email, 
           name = i_name, 
           surname = i_surname, 
           secret_question = i_secret_question, 
           secret_answer = i_secret_answer
             
     where user_id = i_user_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_update_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );