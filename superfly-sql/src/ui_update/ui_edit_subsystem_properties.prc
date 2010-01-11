drop procedure if exists ui_edit_subsystem_properties;
delimiter $$
create procedure ui_edit_subsystem_properties(i_ssys_id int(10),
                                              i_subsystem_name varchar(32),
                                              i_callback_information varchar(64)
)
 main_sql:
  begin
    update subsystems
       set subsystem_name    = coalesce(i_subsystem_name, subsystem_name),
           callback_information   =
             coalesce(i_callback_information, callback_information)
     where ssys_id = i_ssys_id;

    commit;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_edit_subsystem_properties',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );