package com.payneteasy.superfly.security.filters.internal;

import com.payneteasy.superfly.security.filters.ExcludedPaths;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecurityFilterFlowTest {

    private final ExcludedPaths paths = new ExcludedPaths("/static");

    @Test
    public void testPathIsNormalized() {
        assertEquals("/static/a", path("/app", "/app/static/a", "/static", "/a"));
        assertEquals("/admin", path("/app", "/app/static/..;/admin", "/admin", null));
        assertEquals("/static/a", path("/app", "/app/x;jsessionid=1/../static/a", "/static", "/a"));
        assertEquals("/static/a", path("", "/static//a", "/static", "//a"));
        assertEquals("/", path("/app", "/app", "", null));
    }

    @Test
    public void testFallbackToRequestUriWithoutContainerPath() {
        assertEquals("/static/a", path("/app", "/app/./static/a", "", null));
    }

    @Test
    public void testExcludedOnWholeSegmentsOnly() {
        assertTrue(excluded("/app", "/app/static/a", "/static", "/a"));
        assertFalse(excluded("/app", "/app/staticX", "/staticX", null));
        assertFalse(excluded("/app", "/app/static/..;/admin", "/admin", null));
        assertFalse(excluded("/app", "/app/x;jsessionid=1/static", "/x", "/static"));
        assertTrue(excluded("/app", "/app/x;jsessionid=1/../static/a", "/static", "/a"));
    }

    @Test
    public void testDotsAboveTheRootAreNotExcluded() {
        assertFalse(paths.isExcluded(SecurityFilterFlow.normalize("/../static/a")));
        assertFalse(paths.isExcluded(SecurityFilterFlow.normalize("/../../static/a")));
        assertFalse(paths.isExcluded(SecurityFilterFlow.normalize("/a/../../static")));
        assertTrue(paths.isExcluded(SecurityFilterFlow.normalize("/a/../static")));
    }

    @Test
    public void testEncodedSlashIsNotExcluded() {
        assertFalse(excluded("/app", "/app/static%2Fa", "/static/a", null));
        assertFalse(excluded("/app", "/app/static%2f..%2fadmin", "/admin", null));
        assertFalse(excluded("/app", "/app/static%5Ca", "/static\\a", null));
    }

    private boolean excluded(String context, String uri, String servletPath, String pathInfo) {
        return paths.isExcluded(path(context, uri, servletPath, pathInfo));
    }

    private static String path(String context, String uri, String servletPath, String pathInfo) {
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn(context).anyTimes();
        expect(request.getRequestURI()).andReturn(uri).anyTimes();
        expect(request.getServletPath()).andReturn(servletPath).anyTimes();
        expect(request.getPathInfo()).andReturn(pathInfo).anyTimes();
        replay(request);
        return SecurityFilterFlow.getApplicationPath(request);
    }
}
