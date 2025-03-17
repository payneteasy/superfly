package com.payneteasy.superfly.web.wicket.page.session;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.session.UISession;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.user.UserDetailsPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

/**
 * Lists invalid and expired sessions. Also allows to expire invalid sessions
 * and delete expired sessions.
 *
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ListSessionsPage extends BasePage {

    @SpringBean
    private SessionService sessionService;

    public ListSessionsPage() {
        super(ListSessionsPage.class);

        final IModel<List<UISession>> invalidSessionsModel = new LoadableDetachableModel<>() {
            @Override
            protected List<UISession> load() {
                return sessionService.getInvalidSessions();
            }
        };
        add(new ListView<>("invalid-sessions", invalidSessionsModel) {
            @Override
            protected void populateItem(ListItem<UISession> item) {
                fillSessionListItem(item);
            }
        });

        final IModel<List<UISession>> expiredSessionsModel = new LoadableDetachableModel<>() {
            @Override
            protected List<UISession> load() {
                return sessionService.getExpiredSessions();
            }
        };
        add(new ListView<>("expired-sessions", expiredSessionsModel) {
            @Override
            protected void populateItem(ListItem<UISession> item) {
                fillSessionListItem(item);
            }
        });

        add(new Link<Void>("expire-invalid-sessions-link") {
            @Override
            public void onClick() {
                RoutineResult result = sessionService.expireInvalidSessions();
                if (result.isOk()) {
                    info("Invalid sessions expired");
                } else {
                    error("Could not expire invalid sessions: " + result.getErrorMessage());
                }
            }
        });
        add(new Link<Void>("delete-expired-sessions-link") {
            @Override
            public void onClick() {
                sessionService.deleteExpiredSessionsAndNotify();
                info("Deleted expired sessions");
            }
        });
    }

    @Override
    protected String getTitle() {
        return "Sessions";
    }

    private void fillSessionListItem(ListItem<UISession> item) {
        UISession session = item.getModelObject();
        item.add(new Label("session-id", String.valueOf(session.getId())));
        PageParameters params = new PageParameters();
        params.set("userId", String.valueOf(session.getUserId()));
        BookmarkablePageLink<UserDetailsPage> userLink = new BookmarkablePageLink<>(
                "user-link", UserDetailsPage.class, params);
        item.add(userLink);
        userLink.add(new Label("username", session.getUsername()));
        item.add(new Label("callback-information", session.getCallbackInformation()));
    }

}
