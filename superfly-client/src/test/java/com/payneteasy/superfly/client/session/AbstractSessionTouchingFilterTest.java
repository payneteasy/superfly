package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.client.SessionToucher;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Roman Puchkovskiy
 */
public class AbstractSessionTouchingFilterTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private FilterConfig filterConfig;

    @Before
    public void setUp() {
        request = EasyMock.createStrictMock(HttpServletRequest.class);
        response = EasyMock.createStrictMock(HttpServletResponse.class);
        chain = EasyMock.createStrictMock(FilterChain.class);
        filterConfig = EasyMock.createStrictMock(FilterConfig.class);
    }

    @Test
    public void test() throws ServletException, IOException {
        final TestSessionToucher toucher = new TestSessionToucher();
        AbstractSessionTouchingFilter filter;

        chain.doFilter(request, response);
        EasyMock.expectLastCall();
        EasyMock.replay(chain);
        filter = createFilterWithSuperflySessionId(toucher, null);
        filter.init(filterConfig);
        filter.doFilter(request, response, chain);
        filter.destroy();
        EasyMock.verify(chain);

        Assert.assertEquals(Collections.<Long>emptyList(), toucher.getTouchedIds());

        EasyMock.reset(chain);
        chain.doFilter(request, response);
        EasyMock.expectLastCall();
        EasyMock.replay(chain);
        filter = createFilterWithSuperflySessionId(toucher, 3L);
        filter.init(filterConfig);
        filter.doFilter(request, response, chain);
        filter.destroy();
        EasyMock.verify(chain);

        Assert.assertEquals(Arrays.asList(3L), toucher.getTouchedIds());
    }

    private AbstractSessionTouchingFilter createFilterWithSuperflySessionId(
            final TestSessionToucher toucher, final Long superflySessionId) {
        return new AbstractSessionTouchingFilter() {
                @Override
                protected SessionToucher createSessionToucher(FilterConfig filterConfig) {
                    return toucher;
                }

                @Override
                protected Long getSuperflySessionId(HttpServletRequest request) {
                    return superflySessionId;
                }
            };
    }

    private static class TestSessionToucher implements SessionToucher {
        private List<Long> touchedIds = new ArrayList<Long>();

        public List<Long> getTouchedIds() {
            return touchedIds;
        }

        @Override
        public void addSessionId(long sessionId) {
            touchedIds.add(sessionId);
        }
    }
}
