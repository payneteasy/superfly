package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

/**
 * @author rpuch
 */
@Secured("ROLE_ADMIN")
public class ViewSmtpServerPage extends AbstractSmtpServerPage {
    @SpringBean
    private SmtpServerService smtpServerService;

    public ViewSmtpServerPage(PageParameters params) {
        super(params);

        long id = PageParametersBuilder.getId(params);
        UISmtpServer server = smtpServerService.getSmtpServer(id);

        ModalWindow modalWindow = new ModalWindow("modal");
        add(modalWindow);
        add(new Label("name", server.getName()));
        add(new Label("host", server.getHost()));
        add(new Label("port", server.getPort() == null ? "-" : String.valueOf(server.getPort())));
        add(new Label("username", server.getUsername() == null ? "-" : server.getUsername()));
        add(new AjaxLink<Void>("password") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                modalWindow
                        .setInitialHeight(40)
                        .setContent(new Label(ModalWindow.CONTENT_ID, server.getPassword()))
                        .show(target);
            }
        });
        add(new Label("ssl", new ResourceModel(server.isSsl() ? "yes" : "no")));

        add(new BookmarkablePageLink<Void>("back-link", ListSmtpServersPage.class));
    }

    @Override
    protected String getTitle() {
        return "SMTP server details";
    }
}
