drop procedure if exists ui_get_role_groups_list_count;
delimiter $$
create procedure ui_get_role_groups_list_count(i_role_id int(10))
 main_sql:
  begin
    select count(1) records_count
      from     roles r
             join
               subsystems ss
             on r.ssys_ssys_id = ss.ssys_id
           left join
             role_groups rg
           on rg.role_role_id = r.role_id
     where role_id = i_role_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_role_groups_list_count',
                              concat_ws(',', 'records_count int')
     );