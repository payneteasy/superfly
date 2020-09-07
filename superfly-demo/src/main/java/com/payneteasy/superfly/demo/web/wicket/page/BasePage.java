package com.payneteasy.superfly.demo.web.wicket.page;

import com.payneteasy.superfly.demo.web.utils.SecurityUtils;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class BasePage extends WebPage {

    public BasePage(PageParameters parameters) {
        super(parameters);

        add(new Label("user_name", SecurityUtils.getUsername()));

        add(new BookmarkablePageLink<Page>("page1", Page1.class));
        add(new BookmarkablePageLink<Page>("page2", Page2.class));
        add(new BookmarkablePageLink<Page>("page3", Page3.class));
        add(new BookmarkablePageLink<Page>("admin-page", AdminPage.class));
        add(new BookmarkablePageLink<Page>("user-page", UserPage.class));
    }

}
