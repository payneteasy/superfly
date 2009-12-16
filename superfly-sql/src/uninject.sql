drop function if exists uninject;
delimiter $$
create function uninject(i_possible_string text)
  returns text
  deterministic
  no sql
  begin
    declare o_result_string   text;
    set o_result_string   :=
          replace(replace(replace(i_possible_string, "'", ""), '"', ''),'\\','');
    return o_result_string;
  end;
$$
delimiter ;