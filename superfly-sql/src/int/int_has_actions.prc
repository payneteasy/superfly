drop function if exists int_has_actions;
delimiter $$
create function int_has_actions(
        i_user_id           int(10),
        i_subsystem_name    varchar(32)
) returns varchar(1) language sql not deterministic
 main_sql:
  begin
    declare v_group_actions int(10);
    declare v_role_actions  int(10);

    -- checking whether user can login to that subsystem
    select count(*)
      into v_group_actions
      from             user_roles ur
                     join
                       roles r
                     on r.role_id = ur.role_role_id
                   join
                     subsystems ss
                   on r.ssys_ssys_id = ss.ssys_id
                 join
                   role_groups rg
                 on rg.role_role_id = r.role_id
               join
                 groups g
               on rg.grop_grop_id = g.grop_id
             join
               group_actions ga
             on ga.grop_grop_id = g.grop_id and g.ssys_ssys_id = ss.ssys_id
           join
             actions a
           on a.ssys_ssys_id = ss.ssys_id and ga.actn_actn_id = a.actn_id
     where ur.user_user_id = i_user_id and ss.subsystem_name = i_subsystem_name;

    if v_group_actions > 0 then
      return 'Y';
    end if;

    select count(*)
      into v_role_actions
      from           user_roles ur
                   join
                     roles r
                   on r.role_id = ur.role_role_id
                 join
                   subsystems ss
                 on r.ssys_ssys_id = ss.ssys_id
               join
                 user_role_actions ura
               on ura.user_user_id = ur.user_user_id
             join
               role_actions ra
             on ura.ract_ract_id = ra.ract_id and ra.role_role_id = r.role_id
           join
             actions a
           on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = ss.ssys_id
     where ur.user_user_id = i_user_id and ss.subsystem_name = i_subsystem_name;

    if v_role_actions > 0 then
      return 'Y';
    end if;

    return 'N';
  end
$$
delimiter ;
