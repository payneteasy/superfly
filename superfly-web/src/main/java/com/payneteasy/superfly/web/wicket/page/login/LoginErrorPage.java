package com.payneteasy.superfly.web.wicket.page.login;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * @author rpuch
 */
public class LoginErrorPage extends WebPage {
    public LoginErrorPage(IModel<String> messageModel) {
        super(messageModel);

        add(new Label("message", messageModel));
    }
}
