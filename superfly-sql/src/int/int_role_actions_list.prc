drop procedure if exists int_role_actions_list;
delimiter $$
create procedure int_role_actions_list(i_start_from int(10),
                                       i_records_count int(10),
                                       i_order_field_number int(10),
                                       i_order_type varchar(4),
                                       i_role_id int(10),
                                       i_mapping_status varchar(1)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select r.role_id, ',
                 '       r.role_name, ',
                 '       ss.subsystem_name, ',
                 '       a.actn_id, ',
                 '       a.action_name, ',
                 '       concat("Already granted thru groups: ", group_concat(v.group_name)) ',
                 '         granted_group_name ',
                 '  from         roles r ',
                 '             join ',
                 '               subsystems ss ',
                 '             on r.ssys_ssys_id = ss.ssys_id ',
                 '           join ',
                 '             actions a ',
                 '           on a.ssys_ssys_id = ss.ssys_id ',
                 '         left join ',
                 '           role_actions ra ',
                 '         on ra.role_role_id = r.role_id and ra.actn_actn_id = a.actn_id ',
                 '       left join ',
                 '         (select rg.role_role_id, a_gr.actn_id, g.grop_id, g.group_name ',
                 '            from       role_groups rg ',
                 '                     join ',
                 '                       groups g ',
                 '                     on rg.grop_grop_id = g.grop_id ',
                 '                   join ',
                 '                     group_actions ga ',
                 '                   on ga.grop_grop_id = g.grop_id ',
                 '                 join ',
                 '                   actions a_gr ',
                 '                 on ga.actn_actn_id = a_gr.actn_id) v ',
                 '       on a.actn_id = v.actn_id and v.role_role_id = r.role_id ',
                 ' where role_id = ',
                 i_role_id,
                 '       and(   (? = "M" and ra.ract_id is not null) ',
                 '           or(? = "U" and ra.ract_id is null) ',
                 '           or coalesce(?, "A") = "A") ',
                 'group by r.role_id, r.role_name, ss.subsystem_name, a.actn_id, a.action_name'
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
    set @mapping_status   = i_mapping_status;
    prepare v_stmt from @v_ddl_statement;
    execute v_stmt using @mapping_status, @mapping_status, @mapping_status;

    deallocate prepare v_stmt;
  end
$$
delimiter ;