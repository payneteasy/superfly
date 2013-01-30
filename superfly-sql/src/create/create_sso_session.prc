drop procedure if exists create_sso_session;
delimiter $$
create procedure create_sso_session(
        i_user_name         varchar(32),
        i_unique_token      varchar(32)
)
 main_sql:
  begin
    declare v_sso_sess_id   int(10);
    declare v_user_id       int(10);

    select user_id
      into v_user_id
      from users u
     where     u.user_name = i_user_name;

    insert into sso_sessions(
        identifier,
        user_user_id,
        created_date
    ) values (
        i_unique_token,
        v_user_id,
        now()
    );

    set v_sso_sess_id = last_insert_id();

    select v_sso_sess_id sso_session_id, i_unique_token identifier;
  end
$$
delimiter ;
call save_routine_information('create_sso_session',
                              concat_ws(',',
                                        'sso_session_id int',
                                        'identifier varchar'
                              )
     );
