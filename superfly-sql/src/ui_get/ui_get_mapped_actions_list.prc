drop procedure if exists ui_get_mapped_actions_list;
delimiter $$
create procedure ui_get_mapped_actions_list(i_grop_id int(10))
 main_sql:
  begin
    select g.group_name,
           a.action_name,
           a.action_description,
           "Mapped" mapping_status
      from actions a, groups g, group_actions ga
     where     g.ssys_ssys_id = a.ssys_ssys_id
           and ga.actn_actn_id = a.actn_id
           and ga.grop_grop_id = g.grop_id
           and g.grop_id = i_grop_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_mapped_actions_list',
                              concat_ws(',',
                                        'group_name varchar',
                                        'action_name varchar',
                                        'action_description varchar',
                                        'mapping_status varchar'
                              )
     );