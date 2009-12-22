drop procedure if exists ui_get_mapped_roles_list;
delimiter $$
create procedure ui_get_mapped_roles_list(i_user_id int(10), i_ssys_list text)
 main_sql:
  begin
    select u.user_name, r.role_name, ss.subsystem_name, "Mapped" mapping_status
      from users u, roles r, user_roles ur, subsystems ss
     where     ur.role_role_id = r.role_id
           and ur.user_user_id = u.user_id
           and u.user_id = i_user_id
           and ss.ssys_id = r.ssys_ssys_id
           and instr(concat(',', coalesce(i_ssys_list, ss.ssys_id), ','),
                     concat(',', ss.ssys_id, ',')
              ) > 0;
  end
$$
delimiter ;
call save_routine_information('ui_get_mapped_roles_list',
                              concat_ws(',',
                                        'user_name varchar',
                                        'role_name varchar',
                                        'subsystem_name varchar',
                                        'mapping_status varchar'
                              )
     );