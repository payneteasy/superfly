drop procedure if exists ui_get_unmapped_actions_list;
delimiter $$
create procedure ui_get_unmapped_actions_list(i_grop_id int(10))
 main_sql:
  begin
    select g.group_name,
           a.action_name,
           a.action_description,
           "Unmapped" mapping_status
      from     actions a
             left join
               group_actions ga
             on ga.actn_actn_id = a.actn_id
           left join
             groups g
           on ga.grop_grop_id = g.grop_id
     where     g.ssys_ssys_id = a.ssys_ssys_id
           and g.grop_id = i_grop_id
           and ga.gpac_id is null;
  end
$$
delimiter ;
call save_routine_information('ui_get_unmapped_actions_list',
                              concat_ws(',',
                                        'group_name varchar',
                                        'action_name varchar',
                                        'action_description varchar',
                                        'mapping_status varchar'
                              )
     );