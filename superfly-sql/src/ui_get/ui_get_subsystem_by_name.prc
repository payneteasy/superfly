drop procedure if exists ui_get_subsystem_by_name;
delimiter $$
create procedure ui_get_subsystem_by_name(i_subsystem_name varchar(32))
 main_sql:
  begin
    select ss.ssys_id,
           ss.subsystem_name,
           ss.subsystem_title,
           ss.callback_information,
           ss.fixed,
           ss.allow_list_users,
           smtp.ssrv_id smtp_server_ssrv_id,
           smtp.server_name smtp_server_server_name,
           ss.subsystem_url,
           ss.landing_url,
           ss.login_form_css_url
      from subsystems ss
        left join smtp_servers smtp
          on smtp.ssrv_id = ss.ssrv_ssrv_id
     where ss.subsystem_name = i_subsystem_name;
  end
$$
delimiter ;
call save_routine_information('ui_get_subsystem_by_name',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'subsystem_title varchar',
                                        'callback_information varchar',
                                        'fixed varchar',
                                        'allow_list_users varchar',
                                        'smtp_server_ssrv_id int',
                                        'smtp_server_server_name varchar',
                                        'subsystem_url varchar',
                                        'landing_url varchar',
                                        'login_form_css_url varchar'
                              )
     );