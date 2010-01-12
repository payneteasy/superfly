drop procedure if exists ui_get_all_user_actions_list;
delimiter $$
create procedure ui_get_all_user_actions_list(i_start_from int(10),
                                              i_records_count int(10),
                                              i_order_field_number int(10),
                                              i_order_type varchar(4),
                                              i_user_id int(10),
                                              i_ssys_list text,
                                              i_action_name varchar(100)
)
 main_sql:
  begin
    call int_user_actions_list(i_start_from,
                               i_records_count,
                               i_order_field_number,
                               i_order_type,
                               i_user_id,
                               i_ssys_list,
                               "A",
                               i_action_name
         );
  end
$$
delimiter ;
call save_routine_information('ui_get_all_user_actions_list',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'subsystem_name varchar',
                                        'actn_id int',
                                        'action_name varchar',
                                        'mapping_status varchar',
                                        'ract_id int',
                                        'role_id int',
                                        'role_name varchar'
                              )
     );