package com.payneteasy.superfly.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;

/**
 * Cached subsystem URLs used to build the Content-Security-Policy header.
 * Reads {@link SubsystemDao} directly (not SubsystemService) to keep SubsystemServiceImpl free of a dependency cycle.
 */
@Service
public class SubsystemOriginCache {

    private static final Logger logger = LoggerFactory.getLogger(SubsystemOriginCache.class);
    private static final Object KEY = new Object();

    /**
     * @param formActionUrls landing and subsystem URLs (allowed as form-action targets)
     * @param styleUrls      login form CSS URLs (allowed as style-src)
     */
    public record Urls(List<String> formActionUrls, List<String> styleUrls) {
        public static final Urls EMPTY = new Urls(List.of(), List.of());
    }

    private final Cache<Object, Urls> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private SubsystemDao subsystemDao;

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    /**
     * Never throws: on a load failure returns empty URLs (base policy). A failure is not cached,
     * so the next call retries the load.
     */
    public Urls getUrls() {
        try {
            return cache.get(KEY, k -> load());
        } catch (RuntimeException e) {
            logger.warn("Cannot load subsystem URLs for CSP, using base policy", e);
            return Urls.EMPTY;
        }
    }

    public void invalidate() {
        cache.invalidateAll();
    }

    private Urls load() {
        List<String> formAction = new ArrayList<>();
        List<String> style = new ArrayList<>();
        for (UISubsystemForList item : subsystemDao.getSubsystems()) {
            UISubsystem subsystem = subsystemDao.getSubsystem(item.getId());
            if (subsystem == null) {
                continue;
            }
            addIfPresent(formAction, subsystem.getLandingUrl());
            addIfPresent(formAction, subsystem.getSubsystemUrl());
            addIfPresent(style, subsystem.getLoginFormCssUrl());
        }
        return new Urls(List.copyOf(formAction), List.copyOf(style));
    }

    private static void addIfPresent(List<String> target, String url) {
        if (url != null && !url.isBlank()) {
            target.add(url);
        }
    }
}
