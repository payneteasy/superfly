package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificationSignatureVerifierTest {

    private static final long NOW = 1700000000000L;
    private static final String TOKEN = "dummy-subsystem-token";
    private static final String REMOTE_ADDR = "127.0.0.1";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final NotificationSignatureVerifier verifier = new NotificationSignatureVerifier();

    @Test
    public void rejectsEverythingWithoutToken() {
        assertFalse(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
        assertFalse(verifier.isAuthentic(unsigned(), REMOTE_ADDR, NOW));
    }

    @Test
    public void acceptsSignatureMadeWithTokenHash() {
        verifier.setSubsystemToken(TOKEN);
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void tokenIsTrimmed() {
        verifier.setSubsystemToken(" " + TOKEN + "\n");
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void rejectsUnsignedWrongAndExpired() {
        verifier.setSubsystemToken(TOKEN);
        assertFalse(verifier.isAuthentic(unsigned(), REMOTE_ADDR, NOW));
        assertFalse(verifier.isAuthentic(signed(SubsystemTokenHashes.hash("other-token"), NOW), REMOTE_ADDR, NOW));
        assertFalse(verifier.isAuthentic(signed(TOKEN, NOW), REMOTE_ADDR, NOW));
        assertFalse(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR,
                NOW + NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS + 1));
    }

    @Test
    public void blankTokenRemovesIt() {
        verifier.setSubsystemToken(TOKEN);
        verifier.setSubsystemToken(" ");
        assertFalse(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void configuresFromInitParameter() {
        verifier.configure(TOKEN, null);
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void configuresFromPropertiesWhenInitParameterIsAbsent() throws Exception {
        verifier.configure(null, properties("notification.secret=" + TOKEN + "\n"));
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void initParameterWinsOverProperties() throws Exception {
        verifier.configure(TOKEN, properties("notification.secret=other-token\n"));
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void configureWithoutTokenKeepsTheSetOne() throws Exception {
        verifier.setSubsystemToken(TOKEN);
        verifier.configure(null, properties("notification.allowed.ips=\n"));
        assertTrue(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    @Test
    public void configureWithoutAnythingLeavesItRejecting() {
        verifier.configure(null, null);
        assertFalse(verifier.isAuthentic(signed(SubsystemTokenHashes.hash(TOKEN), NOW), REMOTE_ADDR, NOW));
    }

    private String properties(String content) throws Exception {
        File file = folder.newFile("notification.properties");
        Files.write(file.toPath(), content.getBytes(StandardCharsets.ISO_8859_1));
        return file.getAbsolutePath();
    }

    private static Map<String, String[]> unsigned() {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("superflyNotification", new String[]{"LOGOUT"});
        params.put("superflyLogoutSessionIds", new String[]{"1,2"});
        return params;
    }

    private static Map<String, String[]> signed(String key, long timestamp) {
        Map<String, String[]> params = unsigned();
        params.put(NotificationSignatures.TIMESTAMP_PARAMETER, new String[]{String.valueOf(timestamp)});
        params.put(NotificationSignatures.SIGNATURE_PARAMETER, new String[]{NotificationSignatures.sign(key, params)});
        return params;
    }
}
