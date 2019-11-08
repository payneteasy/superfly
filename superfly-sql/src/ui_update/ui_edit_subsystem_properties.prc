drop procedure if exists ui_edit_subsystem_properties;
delimiter $$
create procedure ui_edit_subsystem_properties(i_ssys_id int(10),
                                              i_subsystem_name varchar(32),
                                              i_subsystem_title varchar(255),
                                              i_callback_information varchar(64),
                                              i_send_callbacks varchar(1),
                                              i_allow_list_users varchar(1),
                                              i_ssrv_id int(10),
                                              i_subsystem_token varchar(64),
                                              i_subsystem_url varchar(255),
                                              i_landing_url varchar(255),
                                              i_login_form_css_url varchar(255)
)
 main_sql:
  begin
    update subsystems
       set subsystem_name     = coalesce(i_subsystem_name, subsystem_name),
           subsystem_title    = i_subsystem_title,
           callback_information   = i_callback_information,
           send_callbacks     = i_send_callbacks,
           allow_list_users   = coalesce(i_allow_list_users, allow_list_users),
           ssrv_ssrv_id = i_ssrv_id,
           subsystem_token = i_subsystem_token,
           subsystem_url = i_subsystem_url,
           landing_url = i_landing_url,
           login_form_css_url = i_login_form_css_url
     where ssys_id = i_ssys_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_edit_subsystem_properties',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );
