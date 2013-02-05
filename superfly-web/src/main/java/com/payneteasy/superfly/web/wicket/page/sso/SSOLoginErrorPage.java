package com.payneteasy.superfly.web.wicket.page.sso;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * @author rpuch
 */
public class SSOLoginErrorPage extends WebPage {
    public SSOLoginErrorPage(IModel<String> messageModel) {
        super(messageModel);

        add(new Label("message", messageModel));
    }
}
