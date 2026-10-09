package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Procedures that build an "in (...)" list from a text parameter accept only comma separated numbers; an empty
 * or NULL list changes nothing. Plain JDBC against a database prepared by create_test_database.sh; the URL can be
 * overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class IdListProcedureGuardTest {

    private static final String PREFIX = "idlist-guard-";
    private static final String BAD_LIST = "1,x";

    private Connection conn;
    private long ssysId;
    private long action1;
    private long action2;
    private long groupId;
    private long roleId;
    private long userId;

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        cleanup();
        ssysId = queryLong("select min(ssys_id) from subsystems");
        action1 = insert("insert into actions (action_name, action_description, ssys_ssys_id, log_action) values ('"
                + PREFIX + "a1', 'd', " + ssysId + ", 'N')");
        action2 = insert("insert into actions (action_name, action_description, ssys_ssys_id, log_action) values ('"
                + PREFIX + "a2', 'd', " + ssysId + ", 'N')");
        groupId = insert("insert into groups (group_name, ssys_ssys_id) values ('" + PREFIX + "g', " + ssysId + ")");
        roleId = insert("insert into roles (role_name, principal_name, ssys_ssys_id) values ('" + PREFIX + "r', '"
                + PREFIX + "r', " + ssysId + ")");
        userId = insert("insert into users (user_name, user_password, is_account_locked, logins_failed, email, name,"
                + " surname, secret_question, secret_answer, hotp_salt, create_date) values ('" + PREFIX
                + "u', 'p', 'N', 0, 'e@example.com', 'n', 's', 'q', 'a', 'h', now())");
    }

    @After
    public void tearDown() throws Exception {
        try {
            cleanup();
        } finally {
            conn.close();
        }
    }

    // ---- bad list: error, nothing changes

    @Test
    public void badListIsRejectedAndNothingChanges() throws Exception {
        linkAll();
        String before = state();
        for (Call call : calls()) {
            try {
                call.run(BAD_LIST);
                fail(call.name + " accepted a non numeric list");
            } catch (SQLException e) {
                assertEquals(call.name, "45000", e.getSQLState());
            }
            assertEquals(call.name, before, state());
        }
        try {
            changeLogLevel("1", BAD_LIST);
            fail("ui_change_actions_log_level accepted a non numeric list");
        } catch (SQLException e) {
            assertEquals("45000", e.getSQLState());
        }
        try {
            changeLogLevel(BAD_LIST, "1");
            fail("ui_change_actions_log_level accepted a non numeric list");
        } catch (SQLException e) {
            assertEquals("45000", e.getSQLState());
        }
        assertEquals(before, state());
    }

    // ---- empty and NULL list: no error, nothing changes

    @Test
    public void emptyAndNullListsChangeNothing() throws Exception {
        linkAll();
        String before = state();
        for (Call call : calls()) {
            call.run("");
            assertEquals(call.name, before, state());
            call.run(null);
            assertEquals(call.name, before, state());
        }
        changeLogLevel("", "");
        changeLogLevel(null, null);
        assertEquals(before, state());
    }

    // ---- valid list: link and unlink work as before

    @Test
    public void validListsLinkAndUnlink() throws Exception {
        callList("int_link_group_actions", groupId, "" + action1 + "," + action2);
        assertEquals(2, count("group_actions where grop_grop_id = " + groupId));
        callList("int_unlink_group_actions", groupId, "" + action1);
        assertEquals(1, count("group_actions where grop_grop_id = " + groupId));
        callList("int_unlink_group_actions", groupId, "" + action2);

        callList("int_link_role_groups", roleId, "" + groupId);
        assertEquals(1, count("role_groups where role_role_id = " + roleId));
        callList("int_unlink_role_groups", roleId, "" + groupId);
        assertEquals(0, count("role_groups where role_role_id = " + roleId));

        callList("int_link_role_actions", roleId, "" + action1 + "," + action2);
        assertEquals(2, count("role_actions where role_role_id = " + roleId));

        callList("int_link_user_roles", userId, "" + roleId);
        assertEquals(1, count("user_roles where user_user_id = " + userId));

        String raList = queryString("select group_concat(ract_id) from role_actions where role_role_id = " + roleId);
        callList("int_link_user_role_actions", userId, raList);
        assertEquals(2, count("user_role_actions where user_user_id = " + userId));
        callList("int_unlink_user_role_actions", userId, raList);
        assertEquals(0, count("user_role_actions where user_user_id = " + userId));

        callList("int_unlink_role_actions", roleId, "" + action1 + "," + action2);
        assertEquals(0, count("role_actions where role_role_id = " + roleId));

        callList("int_unlink_user_roles", userId, "" + roleId);
        assertEquals(0, count("user_roles where user_user_id = " + userId));
    }

    @Test
    public void unlinkRoleActionsAlsoRemovesUserRoleActions() throws Exception {
        linkAll();
        callList("int_unlink_role_actions", roleId, "" + action1);
        assertEquals(1, count("role_actions where role_role_id = " + roleId));
        assertEquals(1, count("user_role_actions where user_user_id = " + userId));
    }

    @Test
    public void changeLogLevelWorksWithOneEmptyList() throws Exception {
        changeLogLevel("" + action1, "");
        assertEquals("Y", logAction(action1));
        assertEquals("N", logAction(action2));
        changeLogLevel(null, "" + action1 + "," + action2);
        assertEquals("N", logAction(action1));
        changeLogLevel("" + action1 + "," + action2, null);
        assertEquals("Y", logAction(action1));
        assertEquals("Y", logAction(action2));
    }

    // ---- helpers

    private interface Runner {
        void run(String list) throws SQLException;
    }

    private static final class Call {
        final String name;
        final Runner runner;

        Call(String name, Runner runner) {
            this.name = name;
            this.runner = runner;
        }

        void run(String list) throws SQLException {
            runner.run(list);
        }
    }

    private Call[] calls() {
        return new Call[]{
                call("int_link_group_actions", groupId),
                call("int_link_role_actions", roleId),
                call("int_link_role_groups", roleId),
                call("int_link_user_roles", userId),
                call("int_link_user_role_actions", userId),
                call("int_unlink_group_actions", groupId),
                call("int_unlink_role_actions", roleId),
                call("int_unlink_role_groups", roleId),
                call("int_unlink_user_role_actions", userId),
                call("int_unlink_user_roles", userId),
        };
    }

    private Call call(String procedure, long id) {
        return new Call(procedure, list -> callList(procedure, id, list));
    }

    private void callList(String procedure, long id, String list) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call " + procedure + "(?, ?)}")) {
            cs.setLong(1, id);
            cs.setString(2, list);
            cs.execute();
        }
    }

    private void changeLogLevel(String on, String off) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call ui_change_actions_log_level(?, ?)}")) {
            cs.setString(1, on);
            cs.setString(2, off);
            cs.execute();
        }
    }

    private void linkAll() throws SQLException {
        String actions = action1 + "," + action2;
        callList("int_link_group_actions", groupId, actions);
        callList("int_link_role_groups", roleId, "" + groupId);
        callList("int_link_role_actions", roleId, actions);
        callList("int_link_user_roles", userId, "" + roleId);
        callList("int_link_user_role_actions", userId,
                queryString("select group_concat(ract_id) from role_actions where role_role_id = " + roleId));
    }

    private String state() throws SQLException {
        return count("group_actions where grop_grop_id = " + groupId)
                + "/" + count("role_groups where role_role_id = " + roleId)
                + "/" + count("role_actions where role_role_id = " + roleId)
                + "/" + count("user_roles where user_user_id = " + userId)
                + "/" + count("user_role_actions where user_user_id = " + userId)
                + "/" + logAction(action1) + logAction(action2);
    }

    private String logAction(long actionId) throws SQLException {
        return queryString("select log_action from actions where actn_id = " + actionId);
    }

    private long count(String fromWhere) throws SQLException {
        return queryLong("select count(*) from " + fromWhere);
    }

    private void cleanup() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("delete ura from user_role_actions ura join users u on u.user_id = ura.user_user_id"
                    + " where u.user_name like '" + PREFIX + "%'");
            st.executeUpdate("delete ur from user_roles ur join users u on u.user_id = ur.user_user_id"
                    + " where u.user_name like '" + PREFIX + "%'");
            st.executeUpdate("delete from users where user_name like '" + PREFIX + "%'");
            st.executeUpdate("delete ra from role_actions ra join roles r on r.role_id = ra.role_role_id"
                    + " where r.role_name like '" + PREFIX + "%'");
            st.executeUpdate("delete rg from role_groups rg join roles r on r.role_id = rg.role_role_id"
                    + " where r.role_name like '" + PREFIX + "%'");
            st.executeUpdate("delete from roles where role_name like '" + PREFIX + "%'");
            st.executeUpdate("delete ga from group_actions ga join groups g on g.grop_id = ga.grop_grop_id"
                    + " where g.group_name like '" + PREFIX + "%'");
            st.executeUpdate("delete from groups where group_name like '" + PREFIX + "%'");
            st.executeUpdate("delete from actions where action_name like '" + PREFIX + "%'");
        }
    }

    private long insert(String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rs = st.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long queryLong(String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private String queryString(String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }
}
