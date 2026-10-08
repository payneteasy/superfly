package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.payneteasy.superfly.web.wicket.SameOriginResourceIsolationPolicyTest.LinkPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Browsers without Sec-Fetch-Site: the Origin, then Referer, header decides. The mock request host is "localhost".
 */
public class SameOriginFetchFallbackTest {

    private WicketTester tester;
    private LinkPage page;

    @Before
    public void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            protected void init() {
                super.init();
                getRequestCycleListeners().add(SameOriginResourceIsolationPolicy.newRequestCycleListener());
            }
        });
        page = tester.startPage(new LinkPage());
    }

    @After
    public void tearDown() {
        tester.destroy();
    }

    @Test
    public void foreignOriginIsRejected() {
        click("Origin", "https://evil.example");

        assertFalse(page.clicked);
        assertEquals(403, tester.getLastResponse().getStatus());
    }

    @Test
    public void foreignRefererIsRejected() {
        click("Referer", "https://evil.example/page?x=1");

        assertFalse(page.clicked);
    }

    @Test
    public void nullOriginIsRejected() {
        click("Origin", "null");

        assertFalse(page.clicked);
    }

    @Test
    public void ownOriginIsAcceptedWhateverTheSchemeAndPort() {
        click("Origin", "https://localhost:8443");

        assertTrue(page.clicked);
    }

    @Test
    public void ownRefererIsAccepted() {
        click("Referer", "http://localhost/admin/users");

        assertTrue(page.clicked);
    }

    @Test
    public void originWinsOverReferer() {
        tester.getRequest().setHeader("Referer", "http://localhost/admin");
        click("Origin", "https://evil.example");

        assertFalse(page.clicked);
    }

    @Test
    public void withoutAnyHeaderTheRequestIsLetThrough() {
        click(null, null);

        assertTrue(page.clicked);
    }

    private void click(String header, String value) {
        tester.getRequest().setMethod("GET");
        if (header != null) {
            tester.getRequest().setHeader(header, value);
        }
        tester.clickLink("lock", false);
    }
}
