package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * @author rpuch
 */
public abstract class CreateEditSmtpServerPanel extends Panel {
    public CreateEditSmtpServerPanel(String id, UISmtpServer server) {
        super(id);

        boolean creating = server == null;
        if (creating) {
            server = new UISmtpServer();
        }
        final UISmtpServer finalServer = server;

        Form<UISmtpServer> form = new Form<UISmtpServer>("form") {
            public void onSubmit() {
                RoutineResult result = saveSmtpServer(finalServer);
                if (!result.isOk()) {
                    error(result.getErrorMessage());
                } else {
                    RequestCycle.get().setResponsePage(ViewSmtpServerPage.class,
                            PageParametersBuilder.createId(finalServer.getId()));
                }
            }
        };
        add(form);

//        form.add(new RequiredTextField<String>("name", new PropertyModel<String>(finalServer, "name")));
//        form.add(new RequiredTextField<String>("host", new PropertyModel<String>(finalServer, "host")));
//        form.add(new TextField<Integer>("name", new PropertyModel<Integer>(finalServer, "name")).setType(Integer.class));
//        form.add(new TextField<String>("username", new PropertyModel<String>(finalServer, "username")));
//        form.add(new TextField<String>("password", new PropertyModel<String>(finalServer, "password")));
        form.add(new LabelTextFieldRow<String>(finalServer, "name", "smtpServer.name", true));
        form.add(new LabelTextFieldRow<String>(finalServer, "host", "smtpServer.host", true));
        LabelTextFieldRow<Integer> portRow = new LabelTextFieldRow<Integer>(finalServer, "port", "smtpServer.port");
        portRow.getTextField().setType(Integer.class);
        form.add(portRow);
        form.add(new LabelTextFieldRow<String>(finalServer, "username", "smtpServer.username"));
        form.add(new LabelTextFieldRow<String>(finalServer, "password", "smtpServer.password"));
        form.add(new LabelTextFieldRow<String>(finalServer, "from", "smtpServer.from", true));

        form.add(new SubmitLink("submit-link"));
        form.add(new Link<Void>("cancel-link") {
            @Override
            public void onClick() {
                RequestCycle.get().setResponsePage(ListSmtpServersPage.class);
            }
        });
    }

    protected abstract RoutineResult saveSmtpServer(UISmtpServer server);
}
