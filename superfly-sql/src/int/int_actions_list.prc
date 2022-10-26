drop procedure if exists int_actions_list;
delimiter $$
create procedure int_actions_list(i_action_name varchar(128),
                                  i_action_description varchar(512),
                                  i_ssys_list text,
                                  out o_search_conditions text
)
 main_sql:
  begin
    declare v_search_conditions   text default '';

    if i_action_name is not null then
      set v_search_conditions   =
            concat(" and a.action_name like '%", i_action_name, "%' ");
    end if;

    if i_action_description is not null then
      set v_search_conditions   =
            concat(v_search_conditions,
                   " and a.action_description like '%",
                   i_action_description,
                   "%' "
            );
    end if;

    if i_ssys_list is not null then
      set v_search_conditions   =
            concat(v_search_conditions,
                   " and instr(concat(',', '",
                   i_ssys_list,
                   "', ','), concat(',', a.ssys_ssys_id, ',')) > 0 "
            );
    end if;

    set o_search_conditions   = coalesce(v_search_conditions, '');
  end
$$
delimiter ;