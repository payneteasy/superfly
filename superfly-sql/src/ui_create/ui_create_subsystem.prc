drop procedure if exists ui_create_subsystem;
delimiter $$
create procedure ui_create_subsystem(i_subsystem_name varchar(32),
                                     i_callback_information varchar(64),
                                     out o_ssys_id int(10)
)
 main_sql:
  begin
    insert into subsystems
          (
             subsystem_name, callback_information
          )
    values (i_subsystem_name, i_callback_information);

    set o_ssys_id   = last_insert_id();

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_subsystem',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );