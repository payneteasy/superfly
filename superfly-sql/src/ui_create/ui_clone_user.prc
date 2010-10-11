drop procedure if exists ui_clone_user;
delimiter $$
create procedure ui_clone_user(i_new_user_name varchar(32),
                               i_new_user_password varchar(128),
                               i_new_user_salt varchar(64),
                               i_new_user_email varchar(255),
                               i_template_user_id int(10),
                               out o_user_id int(10)
)
 main_sql:
  begin
    declare v_user_id   int(10);

    insert into users
          (
             user_name, user_password, email, salt
          )
    values (i_new_user_name, i_new_user_password, i_new_user_email,i_new_user_salt);

    set v_user_id   = last_insert_id();
    set o_user_id   = v_user_id;

    insert into user_roles
          (
             user_user_id, role_role_id
          )
      select v_user_id, role_role_id
        from user_roles
       where user_user_id = i_template_user_id;

    insert into user_role_actions
          (
             user_user_id, ract_ract_id
          )
      select v_user_id, ract_ract_id
        from user_role_actions
       where user_user_id = i_template_user_id;

    insert into user_complects
          (
             user_user_id, comp_comp_id
          )
      select v_user_id, comp_comp_id
        from user_complects
       where user_user_id = i_template_user_id;

    insert into user_preferences
          (
             user_user_id, pref_pref_id, user_value
          )
      select v_user_id, pref_pref_id, user_value
        from user_preferences
       where user_user_id = i_template_user_id;

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