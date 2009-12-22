drop procedure if exists ui_clone_user;
delimiter $$
create procedure ui_clone_user(i_new_user_name varchar(32),
                               i_new_user_password varchar(32),
                               i_templete_user_id int(10)
)
 main_sql:
  begin
    declare v_user_id   int(10);

    insert into users
          (
             user_name, user_password
          )
    values (i_new_user_name, i_new_user_password);

    set v_user_id   = last_insert_id();

    insert into user_roles
          (
             user_user_id, role_role_id
          )
      select v_user_id, role_role_id
        from user_roles
       where user_user_id = i_templete_user_id;

    insert into user_role_actions
          (
             user_user_id, ract_ract_id
          )
      select v_user_id, ract_ract_id
        from user_role_actions
       where user_user_id = i_templete_user_id;

    insert into user_complects
          (
             user_user_id, comp_comp_id
          )
      select v_user_id, comp_comp_id
        from user_complects
       where user_user_id = i_templete_user_id;

    insert into user_preferences
          (
             user_user_id, pref_pref_id, user_value
          )
      select v_user_id, pref_pref_id, user_value
        from user_preferences
       where user_user_id = i_templete_user_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_clone_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );