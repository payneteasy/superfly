drop procedure if exists save_actions;
delimiter $$
create procedure save_actions(i_subsystem_name varchar(32))
 main_sql:
  begin
    declare v_ssys_id   int(10);

    select ssys_id
      into v_ssys_id
      from subsystems ss
     where ss.subsystem_name = i_subsystem_name;

    if v_ssys_id is null then
      select 'Failed' status,
             'Subsystem with given identifier does not exist' error_message;

      leave main_sql;
    end if;

    -- remove old actions
    delete ura
      from       role_actions ra
               join
                 user_role_actions ura
               on ura.ract_ract_id = ra.ract_id
             join
               actions a
             on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = v_ssys_id
           left join
             temp_actions ta
           on ta.action_name = a.action_name
     where ta.action_name is null;

    delete ra
      from     role_actions ra
             join
               actions a
             on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = v_ssys_id
           left join
             temp_actions ta
           on ta.action_name = a.action_name
     where ta.action_name is null;

    delete ga
      from     actions a
             join
               group_actions ga
             on ga.actn_actn_id = a.actn_id
           left join
             temp_actions ta
           on ta.action_name = a.action_name
     where ta.action_name is null and a.ssys_ssys_id = v_ssys_id;

    delete a
      from   actions a
           left join
             temp_actions ta
           on ta.action_name = a.action_name
     where ta.action_name is null and a.ssys_ssys_id = v_ssys_id;

    -- create new actions
    insert into actions
          (
             action_name, action_description, ssys_ssys_id, log_action
          )
      select ta.action_name, ta.action_description, v_ssys_id, "N"
        from   temp_actions ta
             left join
               actions a
             on ta.action_name = a.action_name and a.ssys_ssys_id = v_ssys_id
       where a.actn_id is null;

    -- update old descriptions
    update   temp_actions ta
           join
             actions a
           on ta.action_name = a.action_name and a.ssys_ssys_id = v_ssys_id
       set a.action_description    = ta.action_description
     where ta.action_description <> a.action_description;
		 
    truncate table temp_actions;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('save_actions',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );