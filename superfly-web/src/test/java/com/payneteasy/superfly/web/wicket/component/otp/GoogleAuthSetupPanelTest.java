package com.payneteasy.superfly.web.wicket.component.otp;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * QR-код настройки Google Authenticator строится локально (ревью PR #125): CSP блокирует внешние
 * картинки, а секрет не должен уходить третьей стороне.
 */
public class GoogleAuthSetupPanelTest {

    private static final String PREFIX = "data:image/png;base64,";

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new MockApplication());
    }

    @After
    public void tearDown() {
        tester.destroy();
    }

    @Test
    public void rendersLocalDataUriQrWithoutExternalUrls() {
        Model<String> secret = Model.of("");
        tester.startComponentInPage(new GoogleAuthSetupPanel("panel", "sub", "alice", secret));

        String markup = tester.getLastResponseAsString();
        assertFalse(markup, markup.contains("qrserver"));
        assertTrue(markup, extractSrc(markup).startsWith(PREFIX));
    }

    @Test
    public void qrDecodesBackToOtpAuthUri() throws Exception {
        Model<String> secret = Model.of("");
        tester.startComponentInPage(new GoogleAuthSetupPanel("panel", "sub", "alice", secret));

        String src = extractSrc(tester.getLastResponseAsString());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(
                Base64.getDecoder().decode(src.substring(PREFIX.length()))));
        assertEquals(150, image.getWidth());
        assertEquals(150, image.getHeight());

        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        LuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        String decoded = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source))).getText();

        assertEquals("otpauth://totp/alice:sub?secret=" + secret.getObject() + "&issuer=SuperflySSO", decoded);
    }

    private static String extractSrc(String markup) {
        Matcher m = Pattern.compile("<img[^>]*\\ssrc=\"([^\"]*)\"").matcher(markup);
        assertTrue(markup, m.find());
        return m.group(1);
    }
}
