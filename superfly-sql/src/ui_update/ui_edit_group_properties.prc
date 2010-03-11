drop procedure if exists ui_edit_group_properties;
delimiter $$
create procedure ui_edit_group_properties(i_grop_id int(10),
                                          i_group_name varchar(32)
)
 main_sql:
  begin
    update groups
       set group_name    = i_group_name
     where grop_id = i_grop_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_edit_group_properties',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );