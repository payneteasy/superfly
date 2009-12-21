drop procedure if exists ui_copy_action_properties;
delimiter $$
create procedure ui_copy_action_properties(i_actn_id int(10),
                                           i_templete_actn_id int(10),
                                           i_grant_user_privileges varchar(1)
)
 main_sql:
  begin
    declare v_is_templete_actn_id_exists   varchar(1);
    declare v_is_equal_subsystems          varchar(1);

    set v_is_templete_actn_id_exists   = "N";
    set v_is_equal_subsystems   = "N";

    select "Y"
      into v_is_templete_actn_id_exists
      from actions a
     where a.actn_id = i_templete_actn_id;

    if v_is_templete_actn_id_exists = "N" then
      select 'Failed' status, 'Templete does not exist' error_message;

      leave main_sql;
    end if;

    select "Y"
      into v_is_equal_subsystems
      from actions at, actions a
     where     at.actn_id = i_templete_actn_id
           and a.actn_id = i_actn_id
           and a.ssys_ssys_id = at.ssys_ssys_id;

    if v_is_equal_subsystems = "N" then
      select 'Failed' status, 'Actions have different subsystems' error_message

;

      leave main_sql;
    end if;

    delete from group_actions
     where actn_actn_id = i_actn_id;

    insert into group_actions
          (
             grop_grop_id, actn_actn_id
          )
      select ga.grop_grop_id, i_actn_id
        from group_actions ga
       where ga.actn_actn_id = i_templete_actn_id;

    delete ura
      from user_role_actions ura,
           role_actions ra
     where ura.ract_ract_id = ra.ract_id and ra.actn_actn_id = i_actn_id;

    delete from role_actions
     where actn_actn_id = i_actn_id;

    insert into role_actions
          (
             role_role_id, actn_actn_id
          )
      select ra.role_role_id, i_actn_id
        from role_actions ra
       where ra.actn_actn_id = i_templete_actn_id;

    if i_grant_user_privileges = "Y" then
      insert into user_role_actions
            (
               user_user_id, ract_ract_id
            )
        select ura.user_user_id, nra.ract_id
          from user_role_actions ura, role_actions ra, role_actions nra
         where     ura.ract_ract_id = ra.ract_id
               and ra.actn_actn_id = i_templete_actn_id
               and ra.role_role_id = nra.role_role_id
               and nra.actn_actn_id = i_actn_id;
    end if;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_copy_action_properties',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );