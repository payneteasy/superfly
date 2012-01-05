package com.payneteasy.superfly.dao;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;

import java.util.List;

/**
 * DAO to work with SMTP servers.
 *
 * @author rpuch
 */
public interface SmtpServerDao {
    @AStoredProcedure(name = "ui_get_smtp_servers_list")
    List<UISmtpServerForList> listSmtpServers();

    @AStoredProcedure(name = "ui_get_smtp_server")
    UISmtpServer getSmtpServer(long id);

    @AStoredProcedure(name = "ui_get_smtp_server_by_subsystem_identifier")
    UISmtpServer getSmtpServerBySubsystemIdentifier(String subsystemIdentifier);

    @AStoredProcedure(name = "ui_create_smtp_server")
    RoutineResult createSmtpServer(UISmtpServer server);

    @AStoredProcedure(name = "ui_edit_smtp_server")
    RoutineResult updateSmtpServer(UISmtpServer server);

    @AStoredProcedure(name = "ui_delete_smtp_server")
    RoutineResult deleteSmtpServer(long id);

    @AStoredProcedure(name = "ui_get_smtp_servers_list")
    List<UISmtpServerForFilter> getSmtpServersForFilter();
}
