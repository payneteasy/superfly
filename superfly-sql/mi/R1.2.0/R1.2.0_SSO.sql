call run_install_command('alter table users add column name varchar(32) not null', '42S21');
call run_install_command('alter table users add column surname varchar(32) not null', '42S21');
call run_install_command('alter table users add column secret_question varchar(255) not null', '42S21');
call run_install_command('alter table users add column secret_answer varchar(255) not null', '42S21');
call run_install_command('alter table users modify column user_password varchar(128) not null', '42S21');
call run_install_command('alter table users add column salt varchar(64) not null', '42S21');


drop table if exists user_history;
create table user_history(
  `user_user_id`         int(10) not null,
  `user_password` varchar(128) not null,
  `salt` varchar(64) not null,
  `number_history` int(10) unsigned NOT NULL,                                                                           
  `start_date` datetime DEFAULT NULL,                                                                                   
  `end_date` datetime DEFAULT NULL,                                                                                     
  `update_date` datetime DEFAULT NULL,                                                                                  
  PRIMARY KEY (`user_user_id`,`number_history`),                                                                        
  KEY `idx_user_history_start_end_dates` (`user_user_id`,`start_date`,`end_date`),
  CONSTRAINT `fk_user_history_users` FOREIGN KEY (`user_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;                                                                                    


