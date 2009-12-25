package com.payneteasy.superfly.web.wicket.page;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.payneteasy.superfly.web.wicket.page.action.ActionsList;
import com.payneteasy.superfly.web.wicket.page.group.GroupListPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.SubsystemListPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;

public class BasePage extends WebPage{
	public BasePage(PageParameters params) {
		this();
	}
	public BasePage(){
		
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
        add(new BookmarkablePageLink<SubsystemListPage>("subsystems",SubsystemListPage.class));
        add(new BookmarkablePageLink<ActionsList>("actions",ActionsList.class));
        add(new BookmarkablePageLink<GroupListPage>("groups",GroupListPage.class));
        add(new BookmarkablePageLink<ListUsersPage>("users",ListUsersPage.class));
	}
}
