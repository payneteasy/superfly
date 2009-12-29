drop procedure if exists ui_get_users_list;
delimiter $$
create procedure ui_get_users_list(i_start_from int(10),
                                   i_records_count int(10),
                                   i_order_field_number int(10),
                                   i_order_type varchar(4),
                                   i_user_name varchar(32),
                                   i_role_id int(10),
                                   i_comp_id int(10),
                                   i_ssys_id int(10)
)
 main_sql:
  begin
    declare v_sql_core            text;
    declare v_search_conditions   text;

    call int_users_list(i_user_name,
                        i_role_id,
                        i_comp_id,
                        i_ssys_id,
                        v_search_conditions
         );

    set v_sql_core   =
          concat('select u.user_id, ',
                 '       u.user_name, ',
                 '       u.user_password, ',
                 '       u.is_account_locked, ',
                 '       u.logins_failed, ',
                 '       u.last_login_date ',
                 '  from users u ',
                 ' where true ',
                 coalesce(v_search_conditions, '')
          );

    set @v_ddl_statement   =
          concat(v_sql_core,
                 ' order by ',
                 i_order_field_number,
                 ' ',
                 i_order_type,
                 ' limit ',
                 i_start_from,
                 ', ',
                 i_records_count
          );

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;
call save_routine_information('ui_get_users_list',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'user_password varchar',
                                        'is_account_locked varchar',
                                        'logins_failed int',
                                        'last_login_date date'
                              )
     );