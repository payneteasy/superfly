drop procedure if exists change_user_role;
delimiter $$
create procedure change_user_role(i_user_name varchar(32),
                                  i_new_role_name varchar(32)
)
 main_sql:
  begin
    declare v_role_actions_list   text;
    declare v_role_list_unlink    text;
    declare v_user_id             int(10);
    declare v_role_id             int(10);

    set group_concat_max_len   = 65536;

    select user_id into v_user_id from users where user_name = i_user_name;

    select role_id into v_role_id from roles where role_name = i_role_name;

    select group_concat(role_role_id) into v_role_list_unlink
      from user_roles where user_name = i_user_name;

    if v_role_list_unlink is not null and v_role_list_unlink <> '' then
      call int_unlink_user_roles(v_user_id, v_role_list_unlink);
    end if;

    call int_link_user_roles(v_user_id, concat(v_role_id));


    select group_concat(cast(ra.ract_id as char)) ract_list
      into v_role_actions_list
      from     user_roles ur
             join
               role_actions ra
             on ur.role_role_id = ra.role_role_id
           left join
             user_role_actions ura
           on ura.ract_ract_id = ra.ract_id
              and ura.user_user_id = ur.user_user_id
     where     instr(concat(',', i_role_list_grant_actions, ','),
                     concat(',', ur.role_role_id, ',')
               ) > 0
           and ura.urac_id is null
           and ur.user_user_id = i_user_id;

    if v_role_actions_list is not null and v_role_actions_list <> '' then
      call int_link_user_role_actions(i_user_id, v_role_actions_list);
    end if;

    call ui_check_expired_sessions(null, null, i_user_id, null);

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