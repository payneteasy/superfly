drop procedure if exists ui_get_mapped_role_actions_list_count;
delimiter $$
create procedure ui_get_mapped_role_actions_list_count(i_role_id int(10),
                                                       i_action_name varchar(100)
)
 main_sql:
  begin
    call int_role_actions_list_count(i_role_id, "M", i_action_name);
  end
$$
delimiter ;
call save_routine_information('ui_get_mapped_role_actions_list_count',
                              concat_ws(',', 'records_count int')
     );