drop procedure if exists ui_create_user;
delimiter $$
create procedure ui_create_user(i_user_name varchar(32),
                                i_user_password varchar(32)
)
 main_sql:
  begin
    insert into users
          (
             user_name, user_password
          )
    values (i_user_name, i_user_password);

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