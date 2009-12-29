drop procedure if exists ui_get_mapped_role_actions_list;
delimiter $$
create procedure ui_get_mapped_role_actions_list(i_start_from int(10),
                                                 i_records_count int(10),
                                                 i_order_field_number int(10),
                                                 i_order_type varchar(4),
                                                 i_role_id int(10)
)
 main_sql:
  begin
    call int_role_actions_list(i_start_from,
                               i_records_count,
                               i_order_field_number,
                               i_order_type,
                               i_role_id,
                               "M"
         );
  end
$$
delimiter ;
call save_routine_information('ui_get_mapped_role_actions_list',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'subsystem_name varchar',
                                        'actn_id int',
                                        'action_name varchar',
                                        'mapping_status varchar',
                                        'granted_group_name varchar'
                              )
     );