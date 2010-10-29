drop procedure if exists change_temp_password;
delimiter $$
create procedure change_temp_password(i_user_name varchar(32), 
                                      i_user_password varchar(128) 
                                     )
main_sql:
  begin
    declare v_start_date       datetime default now();
    declare v_user_id int(10);
    declare v_salt varchar(128);

    update users u 
       set u.user_password = i_user_password, 
           u.is_password_temp = 'N' 
           where u.user_name = i_user_name;


    if i_user_password is not null then 

    select user_id,salt into v_user_id,v_salt
      from users where user_name = i_user_name;


    update user_history
       set end_date = v_start_date, update_date = now()
     where user_user_id = v_user_id and end_date > v_start_date;

       
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
             v_salt,
             uh.number_history + 1,
             v_start_date start_date,
             '2999-12-31' end_date,
             null update_date
        from user_history uh
       where uh.user_user_id = v_user_id
             and uh.number_history = (select max(uhl.number_history)
                                         from user_history uhl
                                        where uhl.user_user_id = v_user_id);
 
    end if;


    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('change_temp_password', concat_ws(',', 'status varchar', 'error_message varchar'));

