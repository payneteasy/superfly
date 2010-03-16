package com.payneteasy.superfly.web.wicket.page;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.role.ListRolesPage;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemsPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;

/**
 * Base page which defines a common page template and some common page elements.
 */
public abstract class BasePage extends WebPage {
	public BasePage(PageParameters params) {
		this();
	}
	
	public BasePage(){
		add(new Label("user-name-log",SecurityUtils.getUsername()));
		add(new BookmarkablePageLink<HomePage>("self-link", HomePage.class));
		
		add(new Label("page-title", getTitle()));
		add(new Label("page-head-title", getHeadTitle()));
		
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
        add(new BookmarkablePageLink<ListSubsystemsPage>("subsystems-main-menu-item", ListSubsystemsPage.class));
        add(new BookmarkablePageLink<ListActionsPage>("actions-main-menu-item", ListActionsPage.class));
        add(new BookmarkablePageLink<ListGroupsPage>("groups-main-menu-item", ListGroupsPage.class));
        add(new BookmarkablePageLink<ListUsersPage>("users-main-menu-item", ListUsersPage.class));
        add(new BookmarkablePageLink<ListRolesPage>("roles-main-menu-item", ListRolesPage.class));
        add(new BookmarkablePageLink<ListRolesPage>("sessions-main-menu-item", ListSessionsPage.class));
	}
	
	protected abstract String getTitle();
	
	protected String getHeadTitle() {
		return "Superfly service web ~ " + getHeadTitlePostfix();
	}
	
	protected String getHeadTitlePostfix() {
		return getTitle();
	}
	
}
