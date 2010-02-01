drop procedure if exists ui_change_user_roles;
delimiter $$
create procedure ui_change_user_roles(i_user_id int(10),
                                      i_role_list_link text,
                                      i_role_list_unlink text,
                                      i_role_list_grant_actions text
)
 main_sql:
  begin
    declare v_role_actions_list   text;
    set group_concat_max_len   = 65536;

    if i_role_list_link is not null and i_role_list_link <> '' then
      call int_link_user_roles(i_user_id, i_role_list_link);
    end if;

    if i_role_list_unlink is not null and i_role_list_unlink <> '' then
      call int_unlink_user_roles(i_user_id, i_role_list_unlink);
    end if;

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

    commit;

    call ui_check_expired_sessions(null, null, i_user_id, null);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_change_user_roles',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );