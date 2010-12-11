package com.payneteasy.superfly.web.wicket.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.SuperflySession;
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
	private Class<? extends Page> pageClass;

	public BasePage(Class<? extends Page> pageClass) {
		this.pageClass = pageClass;
		init();
	}

	public BasePage(Class<? extends Page> pageClass, PageParameters params) {
		this.pageClass = pageClass;
		init();
	}

	private void init() {
		add(new Label("user-name-log", SecurityUtils.getUsername()));
		add(new BookmarkablePageLink<HomePage>("self-link", HomePage.class));
		
		add(new Label("page-title", getTitle()));
		add(new Label("page-head-title", getHeadTitle()));
		
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
		
		addLink("subsystems-main-menu-item", ListSubsystemsPage.class);
		addLink("actions-main-menu-item", ListActionsPage.class);
		addLink("groups-main-menu-item", ListGroupsPage.class);
		addLink("users-main-menu-item", ListUsersPage.class);
		addLink("roles-main-menu-item", ListRolesPage.class);
		addLink("sessions-main-menu-item", ListSessionsPage.class);
	}

	protected abstract String getTitle();

	protected String getHeadTitle() {
		return "Superfly service web ~ " + getHeadTitlePostfix();
	}

	protected String getHeadTitlePostfix() {
		return getTitle();
	}

	@Override
	public SuperflySession getSession() {
		return (SuperflySession) super.getSession();
	}

	private void addLink(String id, Class<? extends Page> aPageClass) {
		BookmarkablePageLink<? extends Page> link = new BookmarkablePageLink<Page>(id, aPageClass);
		add(link);
		if (pageClass != null) {
			if (pageClass.equals(aPageClass)) {
				link.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						return "p-nav-selected";
					}

				}));
			}
		}
	}

}
