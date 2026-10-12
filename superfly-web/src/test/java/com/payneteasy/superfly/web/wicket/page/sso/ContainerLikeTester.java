package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.web.wicket.SuperflySession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Runs the SSO login pages as in a servlet container, for tests that go through several login steps.
 * <p>
 * Wicket's MockHttpServletRequest.changeSessionId() invalidates the session and drops all its
 * attributes, unlike a servlet container. Here the container request is wrapped so that the id
 * changes but the attributes stay, as in Jetty/Tomcat. Pages are kept in an HTTP session attribute
 * (as Wicket's in-session store does; the disk store keys pages by an identifier that is a session
 * attribute too) and the real CSRF validator is expected among the beans, so pages and the CSRF token
 * must survive the id change. Pages are not serialized because the injected mocks are not serializable.
 */
class ContainerLikeTester extends WicketTester {

    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName() + ".CSRF_TOKEN";

    /** @param beans the values of the {@code @SpringBean} fields, by field type */
    ContainerLikeTester(Map<Class<?>, Object> beans) {
        super(new ContainerLikeApplication());
        getApplication().getComponentInstantiationListeners().add(new BeanInjector(beans));
    }

    @Override
    protected IPageManagerProvider newTestPageManagerProvider() {
        return new SessionPageManagerProvider(getApplication());
    }

    @Override
    public SuperflySession getSession() {
        return (SuperflySession) super.getSession();
    }

    /** The hidden CSRF field is renamed to "_csrf" only in the markup, so the parameter is set explicitly. */
    void submitWithCsrf(FormTester form) {
        Object token = getHttpSession().getAttribute(CSRF_ATTRIBUTE);
        assertNotNull("CSRF token must be in the session", token);
        getRequest().getPostParameters().setParameterValue("_csrf", (String) token);
        form.submit();
    }

    private static class ContainerLikeApplication extends MockApplication {
        @Override
        protected void init() {
            super.init();
            getResourceSettings().getResourceFinders().add(0, new Path("src/main/java"));
        }

        @Override
        public Session newSession(Request request, Response response) {
            return new SuperflySession(request);
        }

        @Override
        public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
            return super.newWebRequest(new AttributeKeepingRequest(servletRequest), filterPath);
        }
    }

    /** Wicket's page store chain reduced to pages kept as instances in the HTTP session. */
    private static class SessionPageManagerProvider extends DefaultPageManagerProvider {
        SessionPageManagerProvider(Application application) {
            super(application);
        }

        @Override
        protected IPageStore newPersistentStore() {
            return new InSessionPageStore(10);
        }

        @Override
        protected IPageStore newSerializingStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newCryptingStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newAsynchronousStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newCachingStore(IPageStore pageStore) {
            return pageStore;
        }
    }

    /** changeSessionId() as a servlet container does it: a new id, the same attributes. */
    private static class AttributeKeepingRequest extends HttpServletRequestWrapper {
        AttributeKeepingRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String changeSessionId() {
            HttpSession session = getSession(false);
            if (session == null) {
                throw new IllegalStateException("No session");
            }
            Map<String, Object> attributes = new HashMap<>();
            for (String name : Collections.list(session.getAttributeNames())) {
                attributes.put(name, session.getAttribute(name));
            }
            String newId = super.changeSessionId();
            HttpSession renewed = getSession(false);
            attributes.forEach(renewed::setAttribute);
            return newId;
        }
    }

    private static class BeanInjector extends Injector implements IComponentInstantiationListener {
        private final IFieldValueFactory factory;

        BeanInjector(Map<Class<?>, Object> beans) {
            factory = new IFieldValueFactory() {
                @Override
                public Object getFieldValue(Field field, Object fieldOwner) {
                    return beans.get(field.getType());
                }

                @Override
                public boolean supportsField(Field field) {
                    return beans.containsKey(field.getType());
                }
            };
        }

        @Override
        public void onInstantiation(Component component) {
            inject(component);
        }

        @Override
        public void inject(Object object) {
            inject(object, factory);
        }
    }
}
