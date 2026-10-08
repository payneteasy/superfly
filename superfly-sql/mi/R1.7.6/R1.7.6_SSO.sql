-- NULL + 1 is NULL: a NULL hotp_logins_failed counter never reached the lockout threshold
update users set hotp_logins_failed = 0 where hotp_logins_failed is null;
commit;
