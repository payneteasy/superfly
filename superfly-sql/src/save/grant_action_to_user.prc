drop procedure if exists grant_action_to_user;
delimiter $$
create procedure i_subsystem_name (i_subsystem_name varchar(32),
                                   i_user_name varchar(32),
                                   i_action_name varchar(128))
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
      from role_actions ra
             join
                 user_role_actions ura
               on ura.ract_ract_id = ra.ract_id
             join
               actions a
             on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = v_ssys_id
                and ta.action_name = i_action_name
             join users u on u.user_id=ura.user_user_id 
                and u.user_name = i_user_name     
             ;

    insert into user_role_actions
          (
             user_user_id, ract_ract_id
          )
    select u.user_id,ra.ract_id from role_actions ra
             join
               actions a
             on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = v_ssys_id
                and ta.action_name = i_action_name
             join users u on u.user_id=ura.user_user_id 
                and u.user_name = i_user_name
             ;
          
          
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('grant_action_to_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );