package com.payneteasy.superfly.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemView;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;

public class SubsystemOriginCacheTest {

    private SubsystemDao dao;
    private SubsystemOriginCache cache;
    private final AtomicLong now = new AtomicLong(1_000_000L);

    @Before
    public void setUp() {
        dao = EasyMock.createStrictMock(SubsystemDao.class);
        cache = new SubsystemOriginCache();
        cache.setSubsystemDao(dao);
        cache.clock = now::get;
    }

    @Test
    public void secondCallIsServedFromCacheAndInvalidateReloads() {
        expectLoad("https://a.example/land", "https://a.example/sub", "https://a.example/x.css");
        expectLoad("https://b.example/land", "https://b.example/sub", null);
        EasyMock.replay(dao);

        SubsystemOriginCache.Urls first = cache.getUrls();
        assertEquals(List.of("https://a.example/land", "https://a.example/sub"), first.formActionUrls());
        assertEquals(List.of("https://a.example/x.css"), first.styleUrls());
        assertEquals(first, cache.getUrls());

        cache.invalidate();
        assertEquals(List.of("https://b.example/land", "https://b.example/sub"), cache.getUrls().formActionUrls());
        EasyMock.verify(dao);
    }

    @Test
    public void loadFailureGivesEmptyUrlsAndIsRetriedOnlyAfterBackoff() {
        EasyMock.expect(dao.getSubsystems()).andThrow(new IllegalStateException("db down"));
        expectLoad("https://a.example/land", "", "");
        EasyMock.replay(dao);

        SubsystemOriginCache.Urls failed = cache.getUrls();
        assertTrue(failed.formActionUrls().isEmpty() && failed.styleUrls().isEmpty());
        // within the backoff the DAO is not touched (strict mock would fail on an unexpected call)
        now.addAndGet(9_999);
        assertEquals(SubsystemOriginCache.Urls.EMPTY, cache.getUrls());
        now.addAndGet(1);
        assertEquals(List.of("https://a.example/land"), cache.getUrls().formActionUrls());
        EasyMock.verify(dao);
    }

    @Test
    public void invalidateResetsFailureBackoff() {
        EasyMock.expect(dao.getSubsystems()).andThrow(new IllegalStateException("db down"));
        expectLoad("https://a.example/land", "", "");
        EasyMock.replay(dao);

        assertEquals(SubsystemOriginCache.Urls.EMPTY, cache.getUrls());
        cache.invalidate();
        assertEquals(List.of("https://a.example/land"), cache.getUrls().formActionUrls());
        EasyMock.verify(dao);
    }

    private void expectLoad(String landing, String subsystemUrl, String css) {
        UISubsystemForList item = new UISubsystemForList();
        item.setId(7L);
        UISubsystemView subsystem = new UISubsystemView();
        subsystem.setLandingUrl(landing);
        subsystem.setSubsystemUrl(subsystemUrl);
        subsystem.setLoginFormCssUrl(css);
        EasyMock.expect(dao.getSubsystems()).andReturn(List.of(item));
        EasyMock.expect(dao.getSubsystem(7L)).andReturn(subsystem);
    }
}
