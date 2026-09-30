package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CSRF для Wicket-ссылок (ревью PR #125): GET-ссылка вроде «заблокировать пользователя»
 * не должна срабатывать при cross-site переходе.
 */
public class SameOriginResourceIsolationPolicyTest {

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
    public void crossSiteNavigationToLinkIsRejected() {
        clickWith("cross-site");

        assertFalse(page.clicked);
        assertEquals(403, tester.getLastResponse().getStatus());
    }

    @Test
    public void sameSiteNavigationToLinkIsRejected() {
        // Соседний поддомен (например, подсистема) — тоже чужой.
        clickWith("same-site");

        assertFalse(page.clicked);
    }

    @Test
    public void sameOriginLinkIsExecuted() {
        clickWith("same-origin");

        assertTrue(page.clicked);
    }

    @Test
    public void browserWithoutFetchMetadataIsLetThrough() {
        clickWith(null);

        assertTrue(page.clicked);
    }

    private void clickWith(String secFetchSite) {
        // Клик по Link в браузере — GET-навигация; мок Wicket по умолчанию шлёт POST.
        tester.getRequest().setMethod("GET");
        if (secFetchSite != null) {
            tester.getRequest().setHeader("Sec-Fetch-Site", secFetchSite);
            tester.getRequest().setHeader("Sec-Fetch-Mode", "navigate");
            tester.getRequest().setHeader("Sec-Fetch-Dest", "document");
        }
        tester.clickLink("lock", false);
    }

    public static class LinkPage extends WebPage implements IMarkupResourceStreamProvider {
        boolean clicked;

        public LinkPage() {
            add(new Link<Void>("lock") {
                @Override
                public void onClick() {
                    clicked = true;
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><a wicket:id=\"lock\">lock</a></body></html>");
        }
    }
}
