package org.apache.commons.ssl;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HttpSecureProtocolTest {
    @Test
    public void testInstantiation() throws GeneralSecurityException, IOException {
        // this test works on JDK8 but failed on JDK9
        new HttpSecureProtocol();
    }
}
