package com.payneteasy.superfly.demo.web.wicket.page;

import java.util.Arrays;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.demo.web.utils.SecurityUtils;

@Secured({"ROLE_ADMINPAGE1", "ROLE_USERPAGE1"})
public class Page1 extends BasePage {

    public Page1(PageParameters parameters) {
        super(parameters);

        add(new Label("username", SecurityUtils.getUsername()));
        add(new ListView<String>("roles", Arrays.asList(SecurityUtils.getRoles())) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String role = item.getModelObject();
                item.add(new Label("role", role));
            }
        });
    }

}
