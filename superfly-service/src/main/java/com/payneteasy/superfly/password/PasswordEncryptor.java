package com.payneteasy.superfly.password;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.hotp.HOTPProviderContextImpl;
import com.payneteasy.superfly.hotp.HOTPProviderUtils;
import com.payneteasy.superfly.hotp.NullHOTPProvider;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPDao;
import com.payneteasy.superfly.spisupport.HOTPData;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.ObjectResolver;
import com.payneteasy.superfly.spisupport.SaltGenerator;

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
        if (args.length != 5) {
            printUsage();
            System.exit(1);
        }

        String algorithm = args[0];
        String url = args[1];
        String username = args[2];
        String password = args[3];
        int hotpCodeDigits = Integer.parseInt(args[4]);

        System.out.println("Please enter hotp master key");
        Scanner scanner = new Scanner(System.in);
        String hotpMasterKey = scanner.nextLine();

        PasswordEncoder passwordEncoder = createPasswordEncoder(algorithm);
        SaltGenerator saltGenerator = createSaltGenerator();

        HOTPProvider hotpProvider = instantiateHOTPProvider(scanner);

        String test = passwordEncoder.encode("password", saltGenerator.generate());

        final Connection conn = createAndInitConnection(url, username, password);

        HOTPProviderContext hotpProviderContext = createHOTPProviderContext(
                hotpMasterKey, hotpCodeDigits, conn);

        hotpProvider.init(hotpProviderContext);
        
        Statement st = conn.createStatement();
        PreparedStatement updatePasswordSt = conn.prepareStatement("update users set user_password = ?, salt = ? where user_id = ?");
        PreparedStatement insertHistorySt = conn.prepareStatement("insert into user_history (user_user_id, user_password, salt, number_history, start_date, end_date) values (?, ?, ?, (select coalesce(max(uh2.number_history), 0) + 1 from user_history uh2 where uh2.user_user_id = ?), now(), '2999-12-31')");
        
        System.out.println("Starting password encryption");
        Set<String> processedNames = new HashSet<String>();
        ResultSet rs = st.executeQuery("select user_id, user_name, user_password from users where (user_password is null or user_password = '' or length(user_password) <> " + test.length() + ")");
        while (rs.next()) {
            long id = rs.getLong("user_id");
            username = rs.getString("user_name");
            password = rs.getString("user_password");
            String salt = saltGenerator.generate();
            String newPassword = passwordEncoder.encode(password != null ? password : "", salt);
//            System.out.println(String.format("User %s (%d): old password '%s', salt '%s', new password '%s'",
//                    username, id, password, salt, newPassword));
            updatePasswordSt.setString(1, newPassword);
            updatePasswordSt.setString(2, salt);
            updatePasswordSt.setLong(3, id);
            updatePasswordSt.addBatch();

            insertHistorySt.setLong(1, id);
            insertHistorySt.setString(2, newPassword);
            insertHistorySt.setString(3, salt);
            insertHistorySt.setLong(4, id);
            insertHistorySt.addBatch();

            processedNames.add(username);
        }
        rs.close();
        st.close();
        
        updatePasswordSt.executeBatch();
        insertHistorySt.executeBatch();

        conn.commit();
        
        updatePasswordSt.close();
        insertHistorySt.close();

        System.out.println("Finished password encryption");
        
        System.out.println("Computing HOTP values for admins");
        
        st = conn.createStatement();
        rs = st.executeQuery("(select user_id, user_name, hotp_counter" +
                " from users u" +
                " join user_roles ur on u.user_id = ur.user_user_id" +
                " join roles r on r.role_id = ur.role_role_id" +
                " join role_groups rg on r.role_id = rg.role_role_id" +
                " join groups g on rg.grop_grop_id = g.grop_id" +
                " join group_actions ga on g.grop_id = ga.actn_actn_id" +
                " join actions a on a.actn_id = ga.actn_actn_id" +
                " where a.action_name = 'admin' and a.ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly'))" +
                " union distinct" +
                " (select user_id, user_name, hotp_counter" +
                " from users u" +
                " join user_roles ur on u.user_id = ur.user_user_id" +
                " join roles r on r.role_id = ur.role_role_id" +
                " join role_actions ra on r.role_id = ra.actn_actn_id" +
                " join actions a on a.actn_id = ra.actn_actn_id" +
                " where a.action_name = 'admin' and a.ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly'))");
        while (rs.next()) {
            username = rs.getString("user_name");
            if (processedNames.contains(username)) {
                long counter = rs.getLong("hotp_counter");
                System.out.println(String.format("HOTP values for %s: %s, %s, %s", username,
                        hotpProvider.computeValue(username, counter),
                        hotpProvider.computeValue(username, counter + 1),
                        hotpProvider.computeValue(username, counter + 2)));
            }
        }
        
        System.out.println("Finished computing HOTP values for admins");
        
        st.close();
        
        conn.close();
    }

    private static HOTPProviderContext createHOTPProviderContext(
            String hotpMasterKey, int hotpCodeDigits, final Connection conn) {
        final HOTPDao hotpDao = createHOTPDao(conn);
        SaltGenerator saltGenerator = createSaltGenerator();
        HOTPService hotpService = createHOTPService();
        ObjectResolver objectResolver = createObjectResolver(hotpDao, saltGenerator, hotpService);
        HOTPProviderContext hotpProviderContext = new HOTPProviderContextImpl(objectResolver, hotpMasterKey, hotpCodeDigits, 10, 100);
        return hotpProviderContext;
    }

    private static HOTPService createHOTPService() {
        return new HOTPService() {
            public void sendTableIfSupported(String subsystemIdentifier, long userId) {
            }

            public void resetTableAndSendIfSupported(String subsystemIdentifier, long userId) {
            }

            @Override
            public String resetGoogleAuthMasterKey(String subsystemIdentifier, String username) throws UserNotFoundException, SsoDecryptException {
                return null;
            }

            @Override
            public String getUrlToGoogleAuthQrCode(String secretKey, String issuer, String accountName) {
                return null;
            }

            @Override
            public boolean validateGoogleTimePassword(String username, String password) {
                return false;
            }

            @Override
            public void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException {

            }
        };
    }

    private static ObjectResolver createObjectResolver(final HOTPDao hotpDao, final SaltGenerator saltGenerator, final HOTPService hotpService) {
        ObjectResolver objectResolver = new ObjectResolver() {
            @SuppressWarnings("unchecked")
            public <T> T resolve(Class<T> clazz) {
                if (clazz == HOTPDao.class) {
                    return (T) hotpDao;
                }
                if (clazz == SaltGenerator.class) {
                    return (T) saltGenerator;
                }
                if (clazz == HOTPService.class) {
                    return (T) hotpService;
                }
                throw new IllegalStateException("Unexpected class requested: " + clazz);
            }
        };
        return objectResolver;
    }

    private static HOTPDao createHOTPDao(final Connection conn) {
        final HOTPDao hotpDao = new HOTPDao() {
            public void updateCounter(String username, long newValue) {
            }

            public HOTPData getHOTPData(String username) {
                PreparedStatement st = null;
                ResultSet rs = null;
                try {
                    st = conn.prepareStatement("select hotp_salt, hotp_counter from users where user_name = ?");
                    st.setString(1, username);
                    rs = st.executeQuery();
                    if (!rs.next()) {
                        return null;
                    }
                    HOTPData data = new HOTPData();
                    data.setUsername(username);
                    data.setSalt(rs.getString("hotp_salt"));
                    data.setCounter(rs.getLong("hotp_counter"));
                    return data;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (st != null) {
                        try {
                            st.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            public void resetHOTP(String username, String hotpSalt) {
            }
        };
        return hotpDao;
    }

    private static HOTPProvider instantiateHOTPProvider(Scanner scanner) {
        HOTPProvider hotpProvider = HOTPProviderUtils.instantiateProvider(false);
        if (hotpProvider == null) {
            System.out.println("Did not find a HOTPProvider provider on the classpath. Are you SURE you want to use null implementation? Be aware that if your Superfly server actually uses a HOTP provider and you don't supply it here, generated HOTP values will be INCORRECT and you will NOT GET ACCESS to your Superfly server!!!");
            System.out.println("Also, be sure that the provider that you supply here is the same as the one that will be used on your Server!");
            System.out.println("So, do you still want to use null provider? (Yes/No)");
            String answer = scanner.nextLine();
            if (!"yes".equalsIgnoreCase(answer)) {
                System.out.println("Exiting");
                System.exit(0);
            }
            hotpProvider = new NullHOTPProvider();
        } else {
            System.out.println("Found the following implementation: " + hotpProvider.getClass().getName());
            System.out.println("Please note that you MUST check CAREFULLY whether this is right, or you may lose control of your server as HOTP codes will be incorrect!");
            System.out.println("Is above implementation correct?");
            String answer = scanner.nextLine();
            if (!"yes".equalsIgnoreCase(answer)) {
                System.out.println("Exiting");
                System.exit(0);
            }
        }
        return hotpProvider;
    }

    private static Connection createAndInitConnection(String url,
            String username, String password) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
        return conn;
    }

    private static void printUsage() {
        System.out.println("Parameters: <algorithm> <db_url> <db_user> <db_password> <hotp_code_digits>");
        System.out.println("  <algorithm>\t\t\thash algorithm (for instance, md-5, sha1, sha-256)");
        System.out.println("  <db_url>\t\t\tURL of the database (for instance, jdbc:mysql://localhost/sso?characterEncoding=utf8");
        System.out.println("  <db_user>\t\t\tusername to connect to database");
        System.out.println("  <db_password>\t\t\tpassword of the database user");
        System.out.println("  <hotp_code_digits>\t\tNumber of digits in HOTP value (must match the value configured in Superfly web-app)");
        System.out.println("Example: sha-256 jdbc:mysql://localhost/sso?characterEncoding=utf8 sso 123sso123 6");
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
