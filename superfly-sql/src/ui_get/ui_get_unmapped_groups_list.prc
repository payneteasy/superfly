drop procedure if exists ui_get_unmapped_group_list;
delimiter $$
create procedure ui_get_unmapped_group_list(i_role_id int(10))
 main_sql:
  begin
    select r.role_name, g.group_name, "Unmapped" mapping_status
      from     groups g
             join
               roles r
             on g.ssys_ssys_id = r.ssys_ssys_id
           left join
             role_groups rg
           on rg.grop_grop_id = g.grop_id and rg.role_role_id = r.role_id
     where r.role_id = i_role_id and rg.rlgp_id is null;
  end
$$
delimiter ;
call save_routine_information('ui_get_unmapped_group_list',
                              concat_ws(',',
                                        'role_name varchar',
                                        'group_name varchar',
                                        'mapping_status varchar'
                              )
     );