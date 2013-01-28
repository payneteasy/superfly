package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.web.wicket.SuperflySession;
import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

/**
 * @author rpuch
 */
public class SessionAccessorPage extends WebPage {
    public SessionAccessorPage() {
    }

    public SessionAccessorPage(IModel<?> model) {
        super(model);
    }

    public SessionAccessorPage(IPageMap pageMap) {
        super(pageMap);
    }

    public SessionAccessorPage(IPageMap pageMap, IModel<?> model) {
        super(pageMap, model);
    }

    public SessionAccessorPage(PageParameters parameters) {
        super(parameters);
    }

    public SessionAccessorPage(IPageMap pageMap, PageParameters parameters) {
        super(pageMap, parameters);
    }

    @Override
   	public SuperflySession getSession() {
   		return (SuperflySession) super.getSession();
   	}
}
