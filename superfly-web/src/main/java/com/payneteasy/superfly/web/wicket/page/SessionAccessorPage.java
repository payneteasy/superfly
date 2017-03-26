package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.web.wicket.SuperflySession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author rpuch
 */
public class SessionAccessorPage extends WebPage {
    public SessionAccessorPage() {
    }

    public SessionAccessorPage(IModel<?> model) {
        super(model);
    }

    public SessionAccessorPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
       public SuperflySession getSession() {
           return (SuperflySession) super.getSession();
       }
}
