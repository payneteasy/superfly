drop procedure if exists int_groups_list;
delimiter $$
create procedure int_groups_list(i_group_name varchar(32),
                                 i_ssys_list text,
                                 out o_search_conditions text
)
 main_sql:
  begin
    declare v_search_conditions   text default '';

    if i_group_name is not null then
      set v_search_conditions   =
            concat(" and g.group_name like '", i_group_name, "%' ");
    end if;

    if i_ssys_list is not null then
      set v_search_conditions   =
            concat(v_search_conditions,
                   " and g.ssys_ssys_id in (",
                   i_ssys_list,
                   ")"
            );
    end if;

    set o_search_conditions   = coalesce(v_search_conditions, '');
  end
$$
delimiter ;