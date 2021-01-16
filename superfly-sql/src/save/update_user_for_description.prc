drop procedure if exists update_user_for_description;
delimiter $$
create procedure update_user_for_description(i_user_id		int(11),
					     i_username		varchar(32),
					     i_email		varchar(255),
					     i_name		varchar(255),
					     i_surname		varchar(255),
					     i_secret_question	varchar(255),
					     i_secret_answer	varchar(255),
					     i_public_key 	text,
					     i_user_organization varchar(255),
					     i_otp_code          varchar(16),
					     i_is_otp_optional varchar(1)
)
 main_sql:
  begin
    update users set
    	email = i_email,
    	name = i_name,
    	surname = i_surname,
    	secret_question = i_secret_question,
    	secret_answer = i_secret_answer,
    	public_key = i_public_key,
        user_organization = i_user_organization
    where user_id = i_user_id;
  end                                                    
$$
delimiter ;
