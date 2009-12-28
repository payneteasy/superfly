drop procedure if exists int_users_list;
delimiter $$
create procedure int_users_list(i_user_name varchar(32),
                                i_role_id int(10),
                                i_comp_id int(10),
                                out o_search_conditions text
)
 main_sql:
  begin
    declare v_search_conditions   text default '';

    if i_user_name is not null then
      set v_search_conditions   =
            concat(" and u.user_name like '", i_user_name, "%' ");
    end if;

    if i_role_id is not null then
      set v_search_conditions   =
            concat(v_search_conditions,
                   " and exists (select 1 from user_roles where role_role_id = ",
                   i_role_id,
                   " and user_user_id = u.user_id)"
            );
    end if;

    if i_comp_id is not null then
      set v_search_conditions   =
            concat(v_search_conditions,
                   " and exists (select 1 ",
                   "               from (select full_path ",
                   "                       from complects ",
                   "                      where comp_id = ",
                   i_comp_id,
                   "                    ) leaf, complects c, user_complects uc ",
                   "              where     (   instr(concat(',', leaf.full_path, ','), concat(',', c.comp_id, ',')) > 0 ",
                   "                         or c.comp_id = ",
                   i_comp_id,
                   "                        ) ",
                   "                    and uc.comp_comp_id = c.comp_id ",
                   "                    and uc.user_user_id = u.user_id)"
            );
    end if;

    set o_search_conditions   = coalesce(v_search_conditions, '');
  end
$$
delimiter ;