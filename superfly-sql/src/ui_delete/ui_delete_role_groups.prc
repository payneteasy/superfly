drop procedure if exists ui_delete_role_groups;
delimiter $$
create procedure ui_delete_role_groups(i_role_id int(10), i_groups_list text)
 main_sql:
  begin
    delete from role_groups
     where role_role_id = i_role_id
           and instr(concat(',', i_groups_list, ','),
                     concat(',', grop_grop_id, ',')
              ) > 0;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_role_groups',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );