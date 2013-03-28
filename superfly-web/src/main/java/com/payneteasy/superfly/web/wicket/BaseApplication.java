package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.MountedMapper;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.UrlPathPageParametersEncoder;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public abstract class BaseApplication extends WebApplication {

    private IPageParametersEncoder parametersEncoder = new PageParametersEncoder();

    protected final void mountBookmarkablePage(String path, Class<? extends WebPage> pageClass) {
        mount(new MountedMapper(path, pageClass, parametersEncoder));
    }

	@Override
	protected final void init() {
        super.init();

		getResourceSettings().addResourceFolder("src/main/java");
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        customInit();
	}

    protected abstract void customInit();

	@Override
	public Session newSession(Request request, Response response) {
		return new SuperflySession(request);
	}

}
