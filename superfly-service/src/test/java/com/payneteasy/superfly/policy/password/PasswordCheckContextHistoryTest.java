package com.payneteasy.superfly.policy.password;

import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordCheckContextHistoryTest {
    private final MessageDigestPasswordEncoder legacy = new MessageDigestPasswordEncoder();

    private static PasswordSaltPair pair(String password, String salt) {
        PasswordSaltPair p = new PasswordSaltPair();
        p.setPassword(password);
        p.setSalt(salt);
        return p;
    }

    private boolean exists(String candidate, PasswordSaltPair... history) {
        return new PasswordCheckContext(candidate, legacy, Arrays.asList(history)).isPasswordExist(candidate, 4);
    }

    @Test
    public void legacyRecordBlocksReuse() {
        PasswordSaltPair rec = pair(legacy.encode("Secret1!", "s1"), "s1");
        assertTrue(exists("Secret1!", rec));
        assertFalse(exists("Other1!", rec));
    }

    @Test
    public void pbkdf2RecordBlocksReuse() {
        PasswordSaltPair rec = pair(new Pbkdf2PasswordEncoder().encode("Secret1!", "s1"), "s1");
        assertTrue(exists("Secret1!", rec));
        assertFalse(exists("Other1!", rec));
    }

    @Test
    public void mixedHistory() {
        PasswordSaltPair old = pair(legacy.encode("Old1!aaa", "s0"), "s0");
        PasswordSaltPair cur = pair(new Pbkdf2PasswordEncoder().encode("New1!aaa", "s1"), "s1");
        assertTrue(exists("Old1!aaa", cur, old));
        assertTrue(exists("New1!aaa", cur, old));
        assertFalse(exists("Third1!a", cur, old));
    }
}
