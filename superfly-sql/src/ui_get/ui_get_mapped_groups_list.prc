drop procedure if exists ui_get_mapped_groups_list;
delimiter $$
create procedure ui_get_mapped_groups_list(i_role_id int(10))
 main_sql:
  begin
    select r.role_name, g.group_name, "Mapped" mapping_status
      from groups g, roles r, role_groups rg
     where     g.ssys_ssys_id = r.ssys_ssys_id
           and rg.grop_grop_id = g.grop_id
           and rg.role_role_id = r.role_id
           and r.role_id = i_role_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_mapped_groups_list',
                              concat_ws(',',
                                        'group_name varchar',
                                        'action_name varchar',
                                        'mapping_status varchar'
                              )
     );