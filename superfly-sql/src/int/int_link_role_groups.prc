drop procedure if exists int_link_role_groups;
delimiter $$
create procedure int_link_role_groups(i_role_id int(10), i_groups_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('insert into role_groups ',
                 '      ( ',
                 '         role_role_id, grop_grop_id ',
                 '      ) ',
                 '  select r.role_id, g.grop_id ',
                 '    from     roles r ',
                 '           join ',
                 '             groups g ',
                 '           on g.ssys_ssys_id = r.ssys_ssys_id ',
                 '              and g.grop_id in (',
                 coalesce(i_groups_list, '-1'),
                 ')',
                 '         left join ',
                 '           role_groups rg ',
                 '         on rg.grop_grop_id = g.grop_id and rg.role_role_id = r.role_id ',
                 '   where rg.rlgp_id is null and r.role_id = ',
                 i_role_id
          );


    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;
call save_routine_information('int_link_role_groups',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );