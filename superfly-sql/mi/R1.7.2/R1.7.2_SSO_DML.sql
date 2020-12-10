
delete from otp_types; 

insert into otp_types (otp_name,otp_code) values ('none','none');
insert into otp_types (otp_name,otp_code) values ('Google Auth','google_auth');

alter table actions_log modify ip_address varchar(64);
alter table unauthorised_access modify ip_address varchar(64);

commit;
