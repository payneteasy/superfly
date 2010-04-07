drop procedure if exists ui_update_user;
delimiter $$
create procedure ui_update_user(i_user_id int(10),
                                i_user_name varchar(32), -- dummy field for java...
                                i_user_password varchar(32),
                                i_user_email varchar(255)
)
 main_sql:
  begin
    update users
       set user_password     = coalesce(i_user_password, user_password), email  = i_user_email
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