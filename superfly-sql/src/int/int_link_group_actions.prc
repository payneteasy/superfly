drop procedure if exists int_link_group_actions;
delimiter $$
create procedure int_link_group_actions(i_grop_id int(10), i_actions_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('    insert into group_actions ',
                 '          ( ',
                 '             grop_grop_id, actn_actn_id ',
                 '          ) ',
                 '      select g.grop_id, actn_id ',
                 '        from     groups g ',
                 '               join ',
                 '                 actions a ',
                 '               on a.ssys_ssys_id = g.ssys_ssys_id ',
                 '                  and a.actn_id in (',
                 coalesce(i_actions_list, '-1'),
                 ') ',
                 '             left join ',
                 '               group_actions ga ',
                 '             on ga.grop_grop_id = g.grop_id and ga.actn_actn_id = a.actn_id ',
                 '       where ga.gpac_id is null and g.grop_id = ',
                 i_grop_id
          );


    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;

  end
$$
delimiter ;