package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.SmtpServerService;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

/**
 * @author rpuch
 */
@Secured("ROLE_ADMIN")
public class CreateSmtpServerPage extends AbstractSmtpServerPage {
    @SpringBean
    private SmtpServerService smtpServerService;

    public CreateSmtpServerPage() {
        add(new CreateEditSmtpServerPanel("create-edit-panel", null) {
            @Override
            protected RoutineResult saveSmtpServer(UISmtpServer server) {
                return smtpServerService.createSmtpServer(server);
            }
        });
    }

    @Override
    protected String getTitle() {
        return "Create SMTP server";
    }
}
