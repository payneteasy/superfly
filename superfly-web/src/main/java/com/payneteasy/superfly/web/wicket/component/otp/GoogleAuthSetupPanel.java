package com.payneteasy.superfly.web.wicket.component.otp;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.http.client.utils.URIBuilder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class GoogleAuthSetupPanel extends Panel {
    private static final int QR_SIZE = 150;

    private final String username;
    private final IModel<String> totpSecret;
    private final String subsystem;

    public GoogleAuthSetupPanel(String id, String subsystem, String username, IModel<String> totpSecret) {
        super(id);
        setOutputMarkupId(true);
        this.subsystem = subsystem;
        this.username = username;
        this.totpSecret = totpSecret;
        this.totpSecret.setObject(generateKey());
        init();
    }

    private void init() {
        final WebMarkupContainer refreshable = new WebMarkupContainer("refreshable");
        add(refreshable.setOutputMarkupId(true));

        WebMarkupContainer markupContainer = new WebMarkupContainer("qr");
        refreshable.add(markupContainer);

        markupContainer.add(new AttributeAppender("src",
                new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        return toQrDataUri(getOtpAuthTotpURL(subsystem, username, totpSecret.getObject()));
                    }
                })
        );

        refreshable.add(new HiddenField<>("key-input", Model.of(totpSecret.getObject()))
                .add(new AttributeModifier("name", "j_key")));
        refreshable.add(new Label("key", totpSecret));
    }

    // Rendered locally: CSP allows only 'self' and data: images, and the secret must not leave the server.
    static String toQrDataUri(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE,
                    Map.of(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name()));
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("No PNG writer available");
            }
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Cannot render QR code", e);
        }
    }

    private String generateKey() {
        return new GoogleAuthenticator().createCredentials().getKey();
    }

    private String getOtpAuthTotpURL(String subsystem,
                                     String accountName,
                                     String secret) {
        URIBuilder uri = new URIBuilder()
                .setScheme("otpauth")
                .setHost("totp")
                .setPath("/" + formatLabel(subsystem, accountName))
                .setParameter("secret", secret)
                .setParameter("issuer", "SuperflySSO");
        return uri.toString();
    }

    private String formatLabel(String subsystem, String accountName) {
        if (accountName == null || accountName.trim().length() == 0) {
            throw new IllegalArgumentException("Account name must not be empty.");
        }

        StringBuilder sb = new StringBuilder(accountName);
        if (subsystem.contains(":")) {
            throw new IllegalArgumentException("Path cannot contain the \':\' character.");
        }
        sb.append(":");
        sb.append(subsystem);

        return sb.toString();
    }

    @Override
    protected void detachModel() {
        super.detachModel();
    }
}
