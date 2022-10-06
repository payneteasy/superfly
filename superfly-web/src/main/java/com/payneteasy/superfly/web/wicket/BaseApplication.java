package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.CsrfPreventionRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.UrlPathPageParametersEncoder;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class BaseApplication extends WebApplication {

    private static final Logger logger = LoggerFactory.getLogger(BaseApplication.class);

    private final IPageParametersEncoder pathParametersEncoder = new UrlPathPageParametersEncoder();
    private final IPageParametersEncoder parametersEncoder = new PageParametersEncoder();

    protected final void mountBookmarkablePageWithPath(String path, Class<? extends WebPage> pageClass) {
        mount(wrapWithInterceptingMapper(new MountedMapper(path, pageClass, pathParametersEncoder)));
    }

    protected final void mountBookmarkablePageWithParameters(String path, Class<? extends WebPage> pageClass) {
        mount(wrapWithInterceptingMapper(new MountedMapper(path, pageClass, parametersEncoder)));
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

        getJavaScriptLibrarySettings().setJQueryReference(
                new UrlResourceReference(Url.parse("https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.1/jquery.min.js"))
        );
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        getRequestCycleListeners().add(new CsrfPreventionRequestCycleListener());
        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory()); // diff key per user
        final IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this);
        setRootRequestMapper(cryptoMapper);
        
        customInit();
    }

    protected abstract void customInit();

    protected IRequestMapper wrapWithInterceptingMapper(IRequestMapper mapper) {
        return mapper;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new SuperflySession(request);
    }

}
