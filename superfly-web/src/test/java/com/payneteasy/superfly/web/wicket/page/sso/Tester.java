package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.web.wicket.SuperflySession;
import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;

import javax.servlet.http.Cookie;
import java.util.Arrays;

/**
 * @author rpuch
 */
public class Tester extends WicketTester {
    public Tester() {
        super(new TestWebApplication());
        ((TestWebApplication) getApplication()).setTester(this);
    }

    public void assertHasCookie(String name, String value) {
        // inspecting request because WicketTester automatically
        // puts all cookies from response to request
        boolean has = false;
        Cookie[] cookies = getRequest().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name) && cookie.getValue().equals(value)) {
                has = true;
                break;
            }
        }
        Assert.assertTrue(String.format("Expected to have cookie %s with name %s but had only the following: %s", name, value,
                Arrays.toString(cookies)), has);
    }

    @Override
    public SuperflySession getSession() {
        return (SuperflySession) super.getSession();
    }

    private static class TestWebApplication extends MockApplication {
        private Tester tester;

        public void setTester(Tester tester) {
            this.tester = tester;
        }

        @Override
        protected void init() {
            super.init();
            getResourceSettings().addResourceFolder("src/main/java");
        }

        @Override
        public Session newSession(Request request, Response response) {
            SuperflySession newSession = new SuperflySession(request);
            if (tester != null) {
                // putting data from old session to new session... ugly, ugly, ugly...
                SuperflySession oldSession = tester.getSession();
                if (oldSession != null) {
                    newSession.setSsoLoginData(oldSession.getSsoLoginData());
                }
            }
            return newSession;
        }
    }
}
