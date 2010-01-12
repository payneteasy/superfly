drop procedure if exists ui_get_unmapped_user_actions_list_count;
delimiter $$
create procedure ui_get_unmapped_user_actions_list_count(i_user_id int(10),
                                                         i_ssys_list text,
                                                         i_action_name varchar(100)
)
 main_sql:
  begin
    call int_user_actions_list_count(i_user_id, i_ssys_list, "U", i_action_name
         );
  end
$$
delimiter ;
call save_routine_information('ui_get_unmapped_user_actions_list_count',
                              concat_ws(',', 'records_count int')
     );