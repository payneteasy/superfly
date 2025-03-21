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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleAuthSetupPanel extends Panel {
    private static final String TOTP_URI_FORMAT =
            "https://chart.googleapis.com/chart?chs=150x150&chld=M%%7C0&cht=qr&chl=%s";

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
                        return String.format(TOTP_URI_FORMAT,
                                URLEncoder.encode(getOtpAuthTotpURL(subsystem, username, totpSecret.getObject()), StandardCharsets.UTF_8));
                    }
                })
        );

        refreshable.add(new HiddenField<>("key-input", Model.of(totpSecret.getObject()))
                .add(new AttributeModifier("name", "j_key")));
        refreshable.add(new Label("key", totpSecret));
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
