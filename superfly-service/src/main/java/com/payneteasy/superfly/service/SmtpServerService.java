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

    /**
     * Returns a server for display and editing, without its password.
     */
    UISmtpServer getSmtpServer(long id);

    /**
     * Returns a server with its decrypted password, for sending mail and for an explicit password view.
     */
    UISmtpServer getSmtpServerWithPassword(long id);

    /**
     * Returns the server of a subsystem with its decrypted password, for sending mail.
     */
    UISmtpServer getSmtpServerBySubsystemIdentifier(String subsystemIdentifier);

    /**
     * Creates a server; the password is stored encrypted and cleared in the passed object.
     */
    RoutineResult createSmtpServer(UISmtpServer server);

    /**
     * Updates a server; an empty password keeps the stored one. The password is stored encrypted and cleared in
     * the passed object.
     */
    RoutineResult updateSmtpServer(UISmtpServer server);

    RoutineResult deleteSmtpServer(long id);

    List<UISmtpServerForFilter> getSmtpServersForFilter();

    /**
     * Audits that the password of a server was shown to the current user.
     */
    void logPasswordViewed(String serverName);
}
