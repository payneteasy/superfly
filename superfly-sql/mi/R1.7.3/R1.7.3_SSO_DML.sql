alter table subsystems add column private_key text;
alter table subsystems add column public_key text;
alter table subsystems add column encryption_algorithm varchar(16);

commit;
