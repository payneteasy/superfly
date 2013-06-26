package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author rpuch
 */
public abstract class AbstractSmtpServerPage extends BasePage {
    public AbstractSmtpServerPage() {
        super(ListSmtpServersPage.class);
    }

    protected AbstractSmtpServerPage(PageParameters params) {
        super(ListSmtpServersPage.class, params);
    }
}
