package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

/**
 * @author rpuch
 */
@Secured("ROLE_ADMIN")
public class ListSmtpServersPage extends AbstractSmtpServerPage {
    @SpringBean
    private SmtpServerService smtpServerService;

    public ListSmtpServersPage() {
    	final ModalWindow testWindow = new ModalWindow("test-window");
    	add(testWindow);
    	
        add(new ListView<UISmtpServerForList>("servers", smtpServerService.listSmtpServers()) {
            @Override
            protected void populateItem(ListItem<UISmtpServerForList> item) {
                final UISmtpServerForList server = item.getModelObject();
                BookmarkablePageLink<Void> viewLink = new BookmarkablePageLink<Void>("view-link",
                        ViewSmtpServerPage.class, PageParametersBuilder.createId(server.getId()));
                item.add(viewLink);
                viewLink.add(new Label("name", server.getName()));
                item.add(new Label("address", server.getPort() == null ? server.getHost() : server.getHost() + ":" + server.getPort()));
                item.add(new BookmarkablePageLink<Void>("edit-link",
                        UpdateSmtpServerPage.class,
                        PageParametersBuilder.createId(server.getId())));
                item.add(new Link<Void>("delete-link") {
                    @Override
                    public void onClick() {
                        RoutineResult result = smtpServerService.deleteSmtpServer(server.getId());
                        if (!result.isOk()) {
                            error(result.getErrorMessage());
                        } else {
                            RequestCycle.get().setResponsePage(ListSmtpServersPage.class);
                            RequestCycle.get().setRedirect(true);
                        }
                    }
                });
                item.add(new AjaxLink<Void>("test-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						testWindow.setContent(new TestPanel(testWindow.getContentId(),
								server.getId(), testWindow, getFeedbackPanel()));
						testWindow.show(target);
					}
				});
            }
        });

        add(new BookmarkablePageLink<Void>("create-server-link", CreateSmtpServerPage.class));
    }

    @Override
    protected String getTitle() {
        return "SMTP servers";
    }
}
