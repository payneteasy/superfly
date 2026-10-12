drop procedure if exists get_subsystem_private_key;
delimiter $$
create procedure get_subsystem_private_key(i_subsystem_name varchar(32))
 main_sql:
  begin
    -- stored encrypted (CryptoService, "v2:"); only remote auth reads it
    -- always exactly one row: NULL for an unknown subsystem or a subsystem without a key
    select (select ss.private_key
              from subsystems ss
             where ss.subsystem_name = i_subsystem_name) as private_key;
  end
$$
delimiter ;
call save_routine_information('get_subsystem_private_key',
                              concat_ws(',',
                                        'private_key varchar'
                              )
     );
