package com.payneteasy.superfly.security.filters;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExcludedPathsTest {

    @Test
    public void testMatchesOnSegmentBoundaries() {
        ExcludedPaths paths = new ExcludedPaths("/static", "/public/");
        assertTrue(paths.isExcluded("/static"));
        assertTrue(paths.isExcluded("/static/"));
        assertTrue(paths.isExcluded("/static/css/app.css"));
        assertTrue(paths.isExcluded("/public"));
        assertTrue(paths.isExcluded("/public/x"));

        assertFalse(paths.isExcluded("/staticX"));
        assertFalse(paths.isExcluded("/static-admin/x"));
        assertFalse(paths.isExcluded("/publicity"));
        assertFalse(paths.isExcluded("/admin/static"));
        assertFalse(paths.isExcluded("/"));
        assertFalse(paths.isExcluded(null));
    }

    @Test
    public void testRootExcludesEverything() {
        ExcludedPaths paths = new ExcludedPaths("/");
        assertTrue(paths.isExcluded("/"));
        assertTrue(paths.isExcluded("/x"));
    }
}
