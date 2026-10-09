package com.payneteasy.superfly.web.wicket.component.otp;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.qrcode.QRCodeReader;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.google.zxing.LuminanceSource;
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
import java.util.Map;
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
        String decoded = decodeQr(src);

        assertEquals("otpauth://totp/alice:sub?secret=" + secret.getObject() + "&issuer=SuperflySSO", decoded);
    }

    @Test
    public void qrOfKnownUriDecodesBack() throws Exception {
        String uri = "otpauth://totp/alice:sub?secret=JBSWY3DPEHPK3PXP&issuer=SuperflySSO";
        assertEquals(uri, decodeQr(GoogleAuthSetupPanel.toQrDataUri(uri)));
    }

    @Test
    public void qrDecodesBackForManyRandomSecrets() throws Exception {
        GoogleAuthenticator authenticator = new GoogleAuthenticator();
        for (int i = 0; i < 200; i++) {
            String uri = "otpauth://totp/alice:sub?secret=" + authenticator.createCredentials().getKey()
                    + "&issuer=SuperflySSO";
            assertEquals("iteration " + i, uri, decodeQr(GoogleAuthSetupPanel.toQrDataUri(uri)));
        }
    }

    private static String decodeQr(String dataUri) throws Exception {
        assertTrue(dataUri, dataUri.startsWith(PREFIX));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(
                Base64.getDecoder().decode(dataUri.substring(PREFIX.length()))));
        assertEquals(150, image.getWidth());
        assertEquals(150, image.getHeight());

        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        LuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(source)), Map.of(
                DecodeHintType.PURE_BARCODE, Boolean.TRUE,
                DecodeHintType.TRY_HARDER, Boolean.TRUE,
                DecodeHintType.CHARACTER_SET, "UTF-8")).getText();
    }

    private static String extractSrc(String markup) {
        Matcher m = Pattern.compile("<img[^>]*\\ssrc=\"([^\"]*)\"").matcher(markup);
        assertTrue(markup, m.find());
        return m.group(1);
    }
}
