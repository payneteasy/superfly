drop procedure if exists register_user;
delimiter $$
create procedure register_user(i_user_name varchar(32),
                                i_user_password varchar(32),
                                i_user_email varchar(255),
                                i_subsystem_name varchar(32),
                                i_principal_list text,
                                out o_user_id int(10)
)
 main_sql:
  begin
    insert into users
          (
             user_name, user_password, email
          )
    values (i_user_name, i_user_password, i_user_email);

    set o_user_id   = last_insert_id();

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('register_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );