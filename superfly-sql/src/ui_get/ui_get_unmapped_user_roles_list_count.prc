drop procedure if exists ui_get_unmapped_user_roles_list_count;
delimiter $$
create procedure ui_get_unmapped_user_roles_list_count(i_user_id int(10), i_ssys_list text)
 main_sql:
  begin
    call int_user_roles_list_count(i_user_id, i_ssys_list, "U");
  end
$$
delimiter ;
call save_routine_information('ui_get_unmapped_user_roles_list_count',
                              concat_ws(',', 'records_count int')
     );