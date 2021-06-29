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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleAuthSetupPanel extends Panel {
    private static final String TOTP_URI_FORMAT =
            "https://chart.googleapis.com/chart?chs=150x150&chld=M%%7C0&cht=qr&chl=%s";

    //private HotpService service;

    private final String username;
    private String email;
    private IModel<String> totpSecret;
    private String issuer;

    public GoogleAuthSetupPanel(String id, String email, String username, IModel<String> totpSecret) {
        super(id);
        setOutputMarkupId(true);
        this.email = email;
        this.issuer = "SSO";
        this.totpSecret = totpSecret;
        this.username = username;
        totpSecret.setObject(generateKey());
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
                        try {
                            return String.format(TOTP_URI_FORMAT,
                                    URLEncoder.encode(getOtpAuthTotpURL(issuer, email, totpSecret.getObject()), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("UTF-8 encoding is not supported by URLEncoder.", e);
                        }
                    }
                })
        );

        refreshable.add(new HiddenField<String>("key-input", totpSecret)
                .add(new AttributeModifier("name", "j_key")));
        refreshable.add(new Label("key", totpSecret));
    }

    private String generateKey() {
        return new GoogleAuthenticator().createCredentials().getKey();
    }

    private String getOtpAuthTotpURL(String issuer,
                                     String accountName,
                                     String secret) {

        URIBuilder uri = new URIBuilder()
                .setScheme("otpauth")
                .setHost("totp")
                .setPath("/" + formatLabel(issuer + "-" + username, accountName))
                .setParameter("secret", secret);


        if (issuer != null) {
            if (issuer.contains(":")) {
                throw new IllegalArgumentException("Issuer cannot contain the \':\' character.");
            }

            uri.setParameter("issuer", issuer);
        }

        return uri.toString();
    }

    private String formatLabel(String issuer, String accountName) {
        if (accountName == null || accountName.trim().length() == 0) {
            throw new IllegalArgumentException("Account name must not be empty.");
        }

        StringBuilder sb = new StringBuilder();
        if (issuer != null) {
            if (issuer.contains(":")) {
                throw new IllegalArgumentException("Issuer cannot contain the \':\' character.");
            }

            sb.append(issuer);
            sb.append(":");
        }

        sb.append(accountName);

        return sb.toString();
    }

    @Override
    protected void detachModel() {
        super.detachModel();
        totpSecret.detach();
    }
}
