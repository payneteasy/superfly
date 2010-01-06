drop procedure if exists ui_get_user;
delimiter $$
create procedure ui_get_user(i_user_id int(10))
 main_sql:
  begin
    select user_id, user_name, user_password from users where user_id = i_user_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_user',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'user_password varchar'
                              )
     );