package com.payneteasy.superfly.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connection settings of the test database. The defaults match the one that dev-env.sh starts;
 * they are overridden with -Dsso.db.url, -Dsso.db.user and -Dsso.db.password.
 */
final class TestDatabase {

    static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3344/ssotest?autoReconnect=true&characterEncoding=utf8&serverTimezone=Europe/Moscow";

    private TestDatabase() {
    }

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("sso.db.url", DEFAULT_URL),
                System.getProperty("sso.db.user", "sso"),
                System.getProperty("sso.db.password", "123sso123"));
    }
}
