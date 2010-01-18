package com.payneteasy.superfly.web.wicket.page;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.role.ListRolesPage;
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
		add(new Label("page-title", getTitle()));
		add(new Label("page-head-title", getHeadTitle()));
		
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
        add(new BookmarkablePageLink<ListSubsystemsPage>("subsystems", ListSubsystemsPage.class));
        add(new BookmarkablePageLink<ListActionsPage>("actions", ListActionsPage.class));
        add(new BookmarkablePageLink<ListGroupsPage>("groups", ListGroupsPage.class));
        add(new BookmarkablePageLink<ListUsersPage>("users", ListUsersPage.class));
        add(new BookmarkablePageLink<ListRolesPage>("role", ListRolesPage.class));
	}
	
	protected abstract String getTitle();
	
	protected String getHeadTitle() {
		return "Superfly service web ~ " + getHeadTitlePostfix();
	}
	
	protected String getHeadTitlePostfix() {
		return getTitle();
	}
	
}
