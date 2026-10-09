-- PCI DSS 2.1: an admin that still has the default password from the documentation must change it at the first login.
-- A password that is already changed (including the pbkdf2 rehash made at login) does not match and stays as is.
update users set is_password_temp = 'Y'
 where user_name = 'admin'
   and user_password = '0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96'
   and salt = '3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f';

-- the HOTP salt of the default admin was published together with the password
update users set hotp_salt = sha1(uuid())
 where user_name = 'admin'
   and hotp_salt = 'f81ead99b99b7f0a91a441621ab1d1248860848f';
