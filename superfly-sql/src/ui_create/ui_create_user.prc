drop procedure if exists ui_create_user;
delimiter $$
create procedure ui_create_user(i_user_name varchar(32),
                                i_user_password varchar(32),
                                i_user_email varchar(255),
                                i_role_id int(10),  
                                out o_user_id int(10)
)
 main_sql:
  begin
    declare v_user_id   int(10);

    insert into users
          (
             user_name, user_password, email, is_account_locked
          )
    values (i_user_name, i_user_password, i_user_email,'N');

    set v_user_id   = last_insert_id();
    set o_user_id   = v_user_id;

    insert into user_roles
          (
            user_user_id, role_role_id
          )
    values (v_user_id, i_role_id);

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