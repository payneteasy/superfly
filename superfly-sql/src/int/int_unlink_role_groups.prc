drop procedure if exists int_unlink_role_groups;
delimiter $$
create procedure int_unlink_role_groups(i_role_id int(10), i_groups_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    if i_groups_list is null or i_groups_list = '' then
      leave main_sql;
    end if;

    if i_groups_list not regexp '^[0-9]+(,[0-9]+)*$' then
      signal sqlstate '45000' set message_text = 'invalid group list';
    end if;

    set v_sql_core   =
          concat('delete from role_groups ',
                 ' where role_role_id = ',
                 i_role_id,
                 '       and grop_grop_id in (',
                 coalesce(i_groups_list, '-1'),
                 ') '
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;