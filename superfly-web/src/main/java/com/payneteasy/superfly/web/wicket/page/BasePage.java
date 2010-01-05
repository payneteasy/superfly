package com.payneteasy.superfly.web.wicket.page;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupPage;
import com.payneteasy.superfly.web.wicket.page.role.ListRolePage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;

public class BasePage extends WebPage{
	public BasePage(PageParameters params) {
		this();
	}
	public BasePage(){
		
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
        add(new BookmarkablePageLink<ListSubsystemPage>("subsystems",ListSubsystemPage.class));
        add(new BookmarkablePageLink<ListActionsPage>("actions",ListActionsPage.class));
        add(new BookmarkablePageLink<ListGroupPage>("groups",ListGroupPage.class));
        add(new BookmarkablePageLink<ListUsersPage>("users",ListUsersPage.class));
        add(new BookmarkablePageLink<ListRolePage>("role",ListRolePage.class));
	}
}
