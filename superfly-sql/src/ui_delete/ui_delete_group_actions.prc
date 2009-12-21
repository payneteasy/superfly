drop procedure if exists ui_delete_group_actions;
delimiter $$
create procedure ui_delete_group_actions(i_grop_id int(10), i_actions_list text
)
 main_sql:
  begin
    delete from group_actions
     where grop_grop_id = i_grop_id
           and instr(concat(',', i_actions_list, ','),
                     concat(',', actn_acnt_id, ',')
              ) > 0;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_group_actions',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );