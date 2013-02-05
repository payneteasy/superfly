drop procedure if exists delete_expired_tokens;
delimiter $$
create procedure delete_expired_tokens(i_max_age_minutes int(10))
 main_sql:
  begin
    declare v_boundary_date datetime;

    set v_boundary_date = date_sub(now(), interval i_max_age_minutes minute);

    delete from subsystem_tokens where created_date < v_boundary_date;
  end
$$
delimiter ;