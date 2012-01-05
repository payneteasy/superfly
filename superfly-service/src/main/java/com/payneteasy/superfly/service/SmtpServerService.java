package com.payneteasy.superfly.service;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;

import java.util.List;

/**
 * Service to work with SMTP servers.
 *
 * @author rpuch
 */
public interface SmtpServerService {
    List<UISmtpServerForList> listSmtpServers();

    UISmtpServer getSmtpServer(long id);

    UISmtpServer getSmtpServerBySubsystemIdentifier(String subsystemIdentifier);

    RoutineResult createSmtpServer(UISmtpServer server);

    RoutineResult updateSmtpServer(UISmtpServer server);

    RoutineResult deleteSmtpServer(long id);

    List<UISmtpServerForFilter> getSmtpServersForFilter();
}
