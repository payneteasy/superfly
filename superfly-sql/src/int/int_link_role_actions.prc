drop procedure if exists int_link_role_actions;
delimiter $$
create procedure int_link_role_actions(i_role_id int(10), i_actions_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('insert into role_actions ',
                 '      ( ',
                 '         role_role_id, actn_actn_id ',
                 '      ) ',
                 '  select r.role_id, a.actn_id ',
                 '    from     roles r ',
                 '           join ',
                 '             actions a ',
                 '           on a.ssys_ssys_id = r.ssys_ssys_id ',
                 '              and a.actn_id in (',
                 coalesce(i_actions_list, '-1'),
                 ')',
                 '         left join ',
                 '           role_actions ra ',
                 '         on ra.actn_actn_id = a.actn_id and ra.role_role_id = r.role_id ',
                 '   where ra.ract_id is null and r.role_id = ',
                 i_role_id
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;