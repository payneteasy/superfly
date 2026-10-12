drop procedure if exists get_subsystems_with_plain_private_key;
delimiter $$
create procedure get_subsystems_with_plain_private_key()
 main_sql:
  begin
    -- keys not yet in the CryptoService "v2:" format; used by the startup encryption task
    select ss.ssys_id,
           ss.subsystem_name,
           ss.private_key
      from subsystems ss
     where ss.private_key is not null
       and ss.private_key <> ''
       and ss.private_key not like 'v2:%';
  end
$$
delimiter ;
call save_routine_information('get_subsystems_with_plain_private_key',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'private_key varchar'
                              )
     );
