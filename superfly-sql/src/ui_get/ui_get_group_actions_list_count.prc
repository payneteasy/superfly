drop procedure if exists ui_get_group_actions_list_count;
delimiter $$
create procedure ui_get_group_actions_list_count(i_grop_id int(10))
 main_sql:
  begin
    select count(1) records_count
      from     groups g
             join
               subsystems ss
             on g.ssys_ssys_id = ss.ssys_id
           left join
             group_actions ga
           on ga.grop_grop_id = g.grop_id
     where grop_id = i_grop_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_group_actions_list_count',
                              concat_ws(',', 'records_count int')
     );