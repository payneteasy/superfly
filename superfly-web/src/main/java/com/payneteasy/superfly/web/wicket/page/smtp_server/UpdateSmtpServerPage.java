package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

/**
 * @author rpuch
 */
@Secured("ROLE_ADMIN")
public class UpdateSmtpServerPage extends AbstractSmtpServerPage {
    @SpringBean
    private SmtpServerService smtpServerService;

    public UpdateSmtpServerPage(PageParameters params) {
        super(params);

        long id = PageParametersBuilder.getId(params);

        add(new CreateEditSmtpServerPanel("create-edit-panel", smtpServerService.getSmtpServer(id)) {
            @Override
            protected RoutineResult saveSmtpServer(UISmtpServer server) {
                return smtpServerService.updateSmtpServer(server);
            }
        });
    }

    @Override
    protected String getTitle() {
        return "Update SMTP server";
    }
}
