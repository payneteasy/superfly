drop procedure if exists change_user_role;
delimiter $$
create procedure change_user_role(i_user_name varchar(32),
                                  i_new_role_name varchar(32),
                                  i_subsystem_name varchar(32)
)
 main_sql:
  begin
    declare v_role_actions_list   text;
    declare v_role_list_unlink    text;
    declare v_user_id             int(10);
    declare v_role_id             int(10);
    declare v_ssys_id             int(10);

    set group_concat_max_len   = 65536;

    select ssys_id into v_ssys_id from subsystems where subsystem_name = i_subsystem_name;

    select user_id into v_user_id from users where user_name = i_user_name;

    select role_id into v_role_id from roles where role_name = i_new_role_name and ssys_ssys_id = v_ssys_id;

    select group_concat(ur.role_role_id) into v_role_list_unlink
      from user_roles ur join users u on ur.user_user_id = u.user_id join roles r on ur.role_role_id = r.role_id
      where u.user_name = i_user_name and r.ssys_ssys_id = v_ssys_id;

    if v_role_list_unlink is not null and v_role_list_unlink <> '' then
      call int_unlink_user_roles(v_user_id, v_role_list_unlink);
    end if;

    call int_link_user_roles(v_user_id, concat(v_role_id));


    call ui_check_expired_sessions(null, null, v_user_id, null);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('change_user_role',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );