drop procedure if exists create_subsystem_token_if_can_login;
delimiter $$
create procedure create_subsystem_token_if_can_login(
        i_sso_sess_id       int(10),
        i_subsystem_name    varchar(32),
        i_unique_token      varchar(32)
)
 main_sql:
  begin
    declare v_sso_sess_id   int(10);
    declare v_user_id       int(10);
    declare v_ssys_id       int(10);
    declare v_group_actions int(10);
    declare v_role_actions  int(10);


    select ssos_id, user_user_id
      into v_sso_sess_id, v_user_id
      from sso_sessions
      where ssos_id = i_sso_sess_id;

    if v_sso_sess_id is null then
      -- no such SSO session - return
      select null from dual where false;
      leave main_sql;
    end if;

    select ssys_id
      into v_ssys_id
      from subsystems
      where subsystem_name = i_subsystem_name;

    if v_ssys_id is null then
      -- no such subsystem - return
      select null from dual where false;
      leave main_sql;
    end if;

    -- now checking whether user can login to that subsystem
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
     where ur.user_user_id = v_user_id and ss.subsystem_name = i_subsystem_name;

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
     where ur.user_user_id = v_user_id and ss.subsystem_name = i_subsystem_name;

    if v_group_actions + v_role_actions = 0 then
      -- no actions in that subsystem - return
      select null from dual where false;
      leave main_sql;
    end if;

    -- removing tokens (for that subsystem) that already exist
    delete from subsystem_tokens
      where ssos_ssos_id = v_sso_sess_id
        and ssys_ssys_id = v_ssys_id;

    insert into subsystem_tokens(
        ssos_ssos_id,
        ssys_ssys_id,
        subsystem_token,
        created_date
    ) values (
        v_sso_sess_id,
        v_ssys_id,
        i_generated_token,
        now()
    );

    select
        i_generated_token subsystem_token,
        landing_url
      from subsystems where ssys_id = v_ssys_id;
  end
$$
delimiter ;
call save_routine_information('create_subsystem_token_if_can_login',
                              concat_ws(',',
                                        'subsystem_token varchar',
                                        'landing_url varchar'
                              )
     );
