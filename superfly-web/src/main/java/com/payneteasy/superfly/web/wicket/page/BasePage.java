package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.role.ListRolesPage;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.smtp_server.ListSmtpServersPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemsPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Base page which defines a common page template and some common page elements.
 */
public abstract class BasePage extends SessionAccessorPage {
    @SpringBean
    private SettingsService settingsService;

    private Class<? extends Page> pageClass;

    private FeedbackPanel feedbackPanel;

    public BasePage(Class<? extends Page> pageClass) {
        super();
        this.pageClass = pageClass;
        init();
    }

    public BasePage(Class<? extends Page> pageClass, PageParameters params) {
        super(params);
        this.pageClass = pageClass;
        init();
    }

    private void init() {
        add(userContainer("user-container"));

        add(new BookmarkablePageLink<Void>("self-link", getApplication().getHomePage()));

        add(new Label("page-title", getTitle()));
        add(new Label("page-head-title", getHeadTitle()));

        add(new Label("superfly-version", settingsService.getSuperflyVersion()));

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        addNavBarItem("subsystems", ListSubsystemsPage.class);
        addNavBarItem("actions", ListActionsPage.class);
        addNavBarItem("groups", ListGroupsPage.class);
        addNavBarItem("users", ListUsersPage.class);
        addNavBarItem("roles", ListRolesPage.class);
        addNavBarItem("sessions", ListSessionsPage.class);
        addNavBarItem("smtp", ListSmtpServersPage.class);


    }

    protected abstract String getTitle();

    protected String getHeadTitle() {
        return "Superfly service web ~ " + getHeadTitlePostfix();
    }

    protected String getHeadTitlePostfix() {
        return getTitle();
    }

    private WebMarkupContainer userContainer(String containerId){
        WebMarkupContainer webMarkupContainer = new WebMarkupContainer(containerId);
        webMarkupContainer.add(new Label("user-name", SecurityUtils.getUsername()));
        return webMarkupContainer;
    }

    private void addNavBarItem(String id, Class<? extends Page> aPageClass) {
        WebMarkupContainer listItemContainer = new WebMarkupContainer("list-item-"+id);
        add(listItemContainer);
        BookmarkablePageLink<? extends Page> link = new BookmarkablePageLink<Page>(id, aPageClass);
        listItemContainer.add(link);
        if (pageClass != null) {
            if (pageClass.equals(aPageClass)) {
                listItemContainer.add(new AttributeModifier("class", Model.of("active")));
            }
        }
    }

    protected FeedbackPanel getFeedbackPanel() {
        return feedbackPanel;
    }

}
