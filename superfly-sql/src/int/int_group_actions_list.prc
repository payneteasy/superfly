drop procedure if exists int_group_actions_list;
delimiter $$
create procedure int_group_actions_list(i_start_from int(10),
                                        i_records_count int(10),
                                        i_order_field_number int(10),
                                        i_order_type varchar(4),
                                        i_grop_id int(10),
                                        i_mapping_status varchar(1)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select g.grop_id, g.group_name, ss.subsystem_name, a.actn_id, a.action_name, if(ga.gpac_id is null, "U", "M") mapping_status ',
                 '  from         groups g ',
                 '             join ',
                 '               subsystems ss ',
                 '             on g.ssys_ssys_id = ss.ssys_id ',
                 '           join ',
                 '             actions a ',
                 '           on a.ssys_ssys_id = ss.ssys_id',
                 '         left join ',
                 '           group_actions ga ',
                 '         on ga.grop_grop_id = g.grop_id ',
                 '            and ga.actn_actn_id = a.actn_id ',
                 ' where grop_id = ',
                 i_grop_id,
                 '       and(   (? = "M" and ga.gpac_id is not null) ',
                 '            or(? = "U" and ga.gpac_id is null) ',
                 '            or coalesce(?, "A") = "A")'
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