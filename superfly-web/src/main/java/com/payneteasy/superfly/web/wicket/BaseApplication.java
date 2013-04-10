package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.UrlPathPageParametersEncoder;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class BaseApplication extends WebApplication {

    private static final Logger logger = LoggerFactory.getLogger(BaseApplication.class);

    private IPageParametersEncoder pathParametersEncoder = new UrlPathPageParametersEncoder();
    private IPageParametersEncoder parametersEncoder = new PageParametersEncoder();

    protected final void mountBookmarkablePageWithPath(String path, Class<? extends WebPage> pageClass) {
        mount(new MountedMapper(path, pageClass, pathParametersEncoder));
    }

    protected final void mountBookmarkablePageWithParameters(String path, Class<? extends WebPage> pageClass) {
        mount(new MountedMapper(path, pageClass, parametersEncoder));
    }

	@Override
	protected final void init() {
        super.init();

        if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
            String path = "src/main/java";
            if (new File(path).exists()) {
                getResourceSettings().getResourceFinders().add(0, new Path(path));
            } else {
                logger.warn("No src/main/java folder found, dynamic resource reloading is not available");
            }
        }
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
