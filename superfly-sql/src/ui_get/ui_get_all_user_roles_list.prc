drop procedure if exists ui_get_all_user_roles_list;
delimiter $$
create procedure ui_get_all_user_roles_list(i_start_from int(10),
                                            i_records_count int(10),
                                            i_order_field_number int(10),
                                            i_order_type varchar(4),
                                            i_user_id int(10),
                                            i_ssys_list text
)
 main_sql:
  begin
    call int_user_roles_list(i_start_from,
                             i_records_count,
                             i_order_field_number,
                             i_order_type,
                             i_user_id,
                             i_ssys_list,
                             "A"
         );
  end
$$
delimiter ;
call save_routine_information('ui_get_all_user_roles_list',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'subsystem_name varchar',
                                        'role_id int',
                                        'role_name varchar',
                                        'mapping_status varchar'
                              )
     );