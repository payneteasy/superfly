drop procedure if exists int_link_user_role_actions;
delimiter $$
create procedure int_link_user_role_actions(i_user_id int(10),
                                            i_role_actions_list text
)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('insert into user_role_actions ',
                 '      ( ',
                 '         user_user_id, ract_ract_id ',
                 '      ) ',
                 '  select u.user_id, ra.ract_id ',
                 '    from       users u ',
                 '             join ',
                 '               user_roles ur ',
                 '             on ur.user_user_id = u.user_id ',
                 '           join ',
                 '             role_actions ra ',
                 '           on     ra.role_role_id = ur.role_role_id ',
                 '              and ra.ract_id in (',
                 coalesce(i_role_actions_list, '-1'),
                 ') ',
                 '         left join ',
                 '           user_role_actions ura ',
                 '         on ra.ract_id = ura.ract_ract_id and ura.user_user_id = u.user_id ',
                 '   where ura.urac_id is null and u.user_id = ',
                 i_user_id
          );


    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;