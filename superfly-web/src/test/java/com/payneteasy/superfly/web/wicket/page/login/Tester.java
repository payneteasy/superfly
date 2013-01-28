package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.web.wicket.SuperflySession;
import junit.framework.Assert;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.WicketTester;

/**
 * @author rpuch
 */
public class Tester extends WicketTester {
    public Tester() {
        super(new TestWebApplication());
    }

    public void assertRedirected(String toUrl) {
        // NB: pretty ugly implementation... for some reason,
        // wicket mock request thinks that all redirects are made
        // inside the same host
        String base;
        String queryString;
        if (toUrl.contains("?")) {
            int index = toUrl.indexOf("?");
            base = toUrl.substring(0, index);
            queryString = toUrl.substring(index + 1);
        } else {
            base = toUrl;
            queryString = null;
        }
        Assert.assertEquals("/" + getApplication().getName()
                + "/" + getApplication().getName()
                + "/" + base,
                getServletRequest().getRequestURI());
        Assert.assertEquals(queryString, getServletRequest().getQueryString());
    }

    private static class TestWebApplication extends BaseWicketTester.DummyWebApplication {
        @Override
        protected void init() {
            super.init();
            getResourceSettings().addResourceFolder("src/main/java");
        }

        @Override
        public Session newSession(Request request, Response response) {
            return new SuperflySession(request);
        }
    }
}
