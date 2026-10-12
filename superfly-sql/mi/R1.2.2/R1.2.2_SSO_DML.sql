delete al from actions_log al join actions a on a.actn_id = al.actn_actn_id where a.action_name='action_temp_password';

delete ga from group_actions ga join actions a on a.actn_id = ga.actn_actn_id where a.action_name='action_temp_password';

delete ura from user_role_actions ura join role_actions ra on ura.ract_ract_id = ra.ract_id
	join actions a on a.actn_id = ra.actn_actn_id where a.action_name='action_temp_password';

delete ra from role_actions ra join actions a on a.actn_id = ra.actn_actn_id where a.action_name='action_temp_password';

delete from actions where action_name='action_temp_password';

-- random, so that the HOTP codes of the default admin are not predictable; an already customised salt is kept
update users set hotp_salt = sha1(uuid()) where user_name = 'admin' and (hotp_salt is null or hotp_salt in ('', 'f81ead99b99b7f0a91a441621ab1d1248860848f'));

commit;
