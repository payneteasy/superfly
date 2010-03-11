drop procedure if exists ui_delete_subsystem;
delimiter $$
create procedure ui_delete_subsystem(i_ssys_id int(10))
 main_sql:
  begin
    declare v_subsystem_name        varchar(128);
    declare v_fixed                 varchar(1);
    declare v_grop_id               int(10);
    declare v_role_id               int(10);
    declare ex_no_records_found     int(10) default 0;

    declare
      cur_groups_to_delete cursor for
        select grop_id
          from groups
         where ssys_ssys_id = i_ssys_id;

    declare
      cur_roles_to_delete cursor for
        select role_id
          from roles
         where ssys_ssys_id = i_ssys_id;

    declare continue handler for not found set ex_no_records_found   = 1;

    select subsystem_name, fixed
      into v_subsystem_name, v_fixed
      from subsystems
     where ssys_id = i_ssys_id;

    if v_fixed = 'Y' then
      select 'OK' status, null error_message;

      leave main_sql;
    end if;

    -- remove actions
    truncate table temp_actions;

    call save_actions(v_subsystem_name);

    -- remove groups
    open cur_groups_to_delete;

    repeat
      fetch cur_groups_to_delete into v_grop_id;

      if not ex_no_records_found then
        call ui_delete_group(v_grop_id);
      end if;
    until ex_no_records_found
    end repeat;

    close cur_groups_to_delete;

    set ex_no_records_found   = 0;

    -- remove roles
    open cur_roles_to_delete;

    repeat
      fetch cur_roles_to_delete into v_role_id;

      if not ex_no_records_found then
        call ui_delete_role(v_role_id);
      end if;
    until ex_no_records_found
    end repeat;

    close cur_roles_to_delete;

    -- !!!TODO!!! remove complects

    delete from subsystems
     where ssys_id = i_ssys_id;

    update sessions
       set session_expired    = 'Y'
     where ssys_ssys_id = i_ssys_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_subsystem',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );