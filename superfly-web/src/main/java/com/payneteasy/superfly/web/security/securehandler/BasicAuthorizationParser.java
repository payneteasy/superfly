package com.payneteasy.superfly.web.security.securehandler;

import org.apache.wicket.util.crypt.Base64;

import java.nio.charset.Charset;

public class BasicAuthorizationParser implements IAuthorizationParser {

    @Override
    public AuthorizationBearer parse(String aText) throws AuthorizationException {

        Tokenizer tokenizer = new Tokenizer(aText, " ");

        String scheme = tokenizer.next("scheme");
        if(!"Basic".equals(scheme)) {
            throw new AuthorizationException("Only Basic scheme supported, but was {}", scheme);
        }

        String base64 = tokenizer.next("base64");

        Tokenizer userPassTokenizer = new Tokenizer(new String( Base64.decodeBase64(base64), Charset.defaultCharset()), ":");
        return new AuthorizationBearer(
                userPassTokenizer.next("user")
                , userPassTokenizer.next("password")
        );
    }
}
