drop procedure if exists get_subsystem_auth;
delimiter $$
create procedure get_subsystem_auth(i_subsystem_name varchar(32))
 main_sql:
  begin
    -- the token never goes through the UI lookups (ui_get_subsystem*): only subsystem authentication reads it
    select ss.ssys_id,
           ss.subsystem_name,
           ss.subsystem_token,
           ss.encryption_algorithm
      from subsystems ss
     where ss.subsystem_name = i_subsystem_name;
  end
$$
delimiter ;
call save_routine_information('get_subsystem_auth',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'subsystem_token varchar',
                                        'encryption_algorithm varchar'
                              )
     );
