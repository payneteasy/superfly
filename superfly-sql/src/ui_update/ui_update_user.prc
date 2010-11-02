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
                                i_salt varchar(64),
                                i_public_key text
)
 main_sql:
  begin
    declare v_start_date       datetime default now();

    update users
       set user_password     = coalesce(i_user_password, user_password),   
           salt = coalesce(i_salt, salt),    
           email  = i_user_email, 
           name = i_name, 
           surname = i_surname, 
           secret_question = i_secret_question, 
           secret_answer = i_secret_answer,
           public_key = i_public_key
     where user_id = i_user_id;

    if i_user_password is not null then 

    update user_history
       set end_date = v_start_date, update_date = now()
     where user_user_id = i_user_id and end_date > v_start_date;

       
    insert into user_history(user_user_id,
                             user_password,
                             salt,
                             number_history,
                             start_date,
                             end_date,
                             update_date
                                 )
      select user_user_id,
             i_user_password,
             i_salt,
             uh.number_history + 1,
             v_start_date start_date,
             '2999-12-31' end_date,
             null update_date
        from user_history uh
       where uh.user_user_id = i_user_id
             and uh.number_history = (select max(uhl.number_history)
                                         from user_history uhl
                                        where uhl.user_user_id = i_user_id);
 
    end if;

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