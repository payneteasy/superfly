package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PageTestLimiterResetTest extends AbstractPageTest {

    @Test
    public void a_testBlocksPairInSharedLimiter() {
        for (int i = 0; i < 5; i++) {
            LoginAttemptLimiter.shared().recordFailure("password", "10.0.0.1", "alice");
        }
        assertTrue(LoginAttemptLimiter.shared().isBlocked("password", "10.0.0.1", "alice"));
    }

    @Test
    public void b_nextTestStartsWithCleanLimiter() {
        assertFalse(LoginAttemptLimiter.shared().isBlocked("password", "10.0.0.1", "alice"));
    }
}
