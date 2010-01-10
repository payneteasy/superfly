set @sv_subsystems_count = 3;
set @sv_actions_count_per_subsystem_max = 1000;
set @sv_groups_count_per_subsystem_max = 100;
set @sv_roles_count_per_subsystem_max = 10;
set @sv_users_count = 50;
set @sv_free_actions_count_per_user_max = 3;

insert into subsystems
      (
         subsystem_name, callback_information, fixed
      )
  select concat('Subsystem - ', rownum),
         concat('Call back link for subsystem - ', rownum), 'N'
    from (select @sv_row_number   := @sv_row_number + 1 rownum
            from information_schema.columns c, (select @sv_row_number   := 0) sv)
         dl
   where rownum <= @sv_subsystems_count;

insert into actions
      (
         action_name, action_description, ssys_ssys_id, log_action
      )
  select concat(ss.subsystem_name, ': action - ', rownum),
         concat(ss.subsystem_name, ': action description - ', rownum),
         ss.ssys_id,
         if(rownum mod 312 = 0, 'Y', 'N')
    from subsystems ss,
         (select rownum
            from (select @sv_row_number   := @sv_row_number + 1 rownum
                    from information_schema.columns c,
                         (select @sv_row_number   := 0) sv) dl
           where rownum <= @sv_actions_count_per_subsystem_max) lc;

insert into groups
      (
         group_name, ssys_ssys_id
      )
  select concat(ss.subsystem_name, ': group - ', rownum),
         ss.ssys_id
    from subsystems ss,
         (select rownum
            from (select @sv_row_number   := @sv_row_number + 1 rownum
                    from information_schema.columns c,
                         (select @sv_row_number   := 0) sv) dl
           where rownum <= @sv_groups_count_per_subsystem_max) lc;

insert into group_actions
      (
         grop_grop_id, actn_actn_id
      )
  select g.grop_id, al.actn_id
    from groups g, (select a.actn_id, a.ssys_ssys_id
                      from actions a
                     where a.actn_id mod (round(100 * rand()) + 3) = 0) al
   where g.ssys_ssys_id = al.ssys_ssys_id
         and((g.grop_id + al.actn_id + round(5 * rand())) mod 2) = 1;

insert into roles
      (
         role_name, principal_name, ssys_ssys_id
      )
  select concat(ss.subsystem_name, ': role - ', rownum),
	       concat(ss.subsystem_name, ': principal - ', rownum),
         ss.ssys_id
    from subsystems ss,
         (select rownum
            from (select @sv_row_number   := @sv_row_number + 1 rownum
                    from information_schema.columns c,
                         (select @sv_row_number   := 0) sv) dl
           where rownum <= @sv_roles_count_per_subsystem_max) lc;

insert into role_groups
      (
         role_role_id, grop_grop_id
      )
  select r.role_id, gl.grop_id
    from roles r, (select g.grop_id, g.ssys_ssys_id
                      from groups g
                     where g.grop_id mod (round(100 * rand()) + 3) = 0) gl
   where r.ssys_ssys_id = gl.ssys_ssys_id
         and((r.role_id + gl.grop_id + round(5 * rand())) mod 2) = 1;

insert into role_actions
      (
         role_role_id, actn_actn_id
      )
  select distinct an.role_id, an.actn_id
    from     (select r.role_id, al.actn_id
                from roles r,
                     (select a.actn_id, a.ssys_ssys_id
                        from actions a
                       where a.actn_id mod (round(500 * rand()) + 3) = 0) al
               where r.ssys_ssys_id = al.ssys_ssys_id
                     and((r.role_id + al.actn_id + round(5 * rand())) mod 2) = 1)
             an
           left join
             role_groups rg
           on rg.role_role_id = an.role_id
         left join
           group_actions ga
         on ga.grop_grop_id = rg.grop_grop_id and ga.actn_actn_id = an.actn_id
   where ga.gpac_id is null;

insert into users
      (
         comp_comp_id, user_name, user_password, is_account_locked, logins_failed, last_login_date
      )
  select null,
         concat('user - ', rownum),
         concat('user password - ', rownum),
         if(rownum mod 13 = 0, 'Y', 'N'),
         5 mod round(6 * rand()),
         now()
    from (select @sv_row_number   := @sv_row_number + 1 rownum
            from information_schema.columns c, (select @sv_row_number   := 0) sv)
         dl
   where rownum <= @sv_users_count;

insert into user_roles
      (
         user_user_id, role_role_id
      )
  select u.user_id, r.role_id
    from users u, roles r
   where (u.user_id + r.role_id + round(10 * rand())) mod 5 = 0;

insert into user_role_actions
      (
         user_user_id, ract_ract_id
      )
  select user_id, ract_id
    from role_actions ra, users u, user_roles ur
   where     ur.user_user_id = u.user_id
         and ur.role_role_id = ra.role_role_id
         and round(60 * rand()) mod 17 = 0;