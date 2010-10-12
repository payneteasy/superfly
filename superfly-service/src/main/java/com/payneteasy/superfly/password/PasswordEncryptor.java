package com.payneteasy.superfly.password;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Encrypts passwords in the database. It assumes they are not encoded yet.
 * Run without parameters to get help.
 * WARNING! If some of your passwords are already encrypted, this may
 * re-encrypt them (i.e. make then non-usable). Encryptor tries to avoid this
 * by skipping users for which password length matches the new encrypted
 * password length, but it's just a heuristic.
 *
 * @author Roman Puchkovskiy
 */
public class PasswordEncryptor {
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			printUsage();
			System.exit(1);
		}
		
		String algorithm = args[0];
		String url = args[1];
		String username = args[2];
		String password = args[3];
		
		PasswordEncoder passwordEncoder = createPasswordEncoder(algorithm);
		SaltGenerator saltGenerator = createSaltGenerator();
		
		String test = passwordEncoder.encode("password", saltGenerator.generate());
		
		Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (url, username, password);
        conn.setAutoCommit(false);
        Statement st = conn.createStatement();
        PreparedStatement updatePasswordSt = conn.prepareStatement("update users set user_password = ?, salt = ? where user_id = ?");
        PreparedStatement insertHistorySt = conn.prepareStatement("insert into user_history (user_user_id, user_password, salt, number_history, start_date, end_date) values (?, ?, ?, 1, now(), '2999-12-31')");
        
        System.out.println("Starting password encryption");
        ResultSet rs = st.executeQuery("select user_id, user_name, user_password from users where (user_password is null or user_password = '' or length(user_password) <> " + test.length() + ")");
        while (rs.next()) {
        	long id = rs.getLong("user_id");
        	username = rs.getString("user_name");
        	password = rs.getString("user_password");
        	String salt = saltGenerator.generate();
        	String newPassword = passwordEncoder.encode(password != null ? password : "", salt);
//        	System.out.println(String.format("User %s (%d): old password '%s', salt '%s', new password '%s'",
//        			username, id, password, salt, newPassword));
        	updatePasswordSt.setString(1, newPassword);
        	updatePasswordSt.setString(2, salt);
        	updatePasswordSt.setLong(3, id);
        	updatePasswordSt.addBatch();
        	
        	insertHistorySt.setLong(1, id);
        	insertHistorySt.setString(2, newPassword);
        	insertHistorySt.setString(3, salt);
        	insertHistorySt.addBatch();
        }
        rs.close();
        st.close();
        
        updatePasswordSt.executeBatch();
        insertHistorySt.executeBatch();

        conn.commit();
        
        updatePasswordSt.close();
        insertHistorySt.close();
        conn.close();
        
        System.out.println("Finished password encryption");
	}

	private static void printUsage() {
		System.out.println("Parameters: <algorithm> <db_url> <db_user> <db_password>");
		System.out.println("  <algorithm>\t\thash algorithm (for instance, md-5, sha1, sha-256)");
		System.out.println("  <db_url>\t\tURL of the database (for instance, jdbc:mysql://localhost/sso?characterEncoding=utf8");
		System.out.println("  <db_user>\t\tusername to connect to database");
		System.out.println("  <db_password>\t\tpassword of the database user");
		System.out.println("Example: sha-256 jdbc:mysql://localhost/sso?characterEncoding=utf8 sso 123sso123");
	}

	private static SHA256RandomGUIDSaltGenerator createSaltGenerator() {
		return new SHA256RandomGUIDSaltGenerator();
	}

	private static MessageDigestPasswordEncoder createPasswordEncoder(String algorithm) {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm(algorithm);
		return encoder;
	}
}
