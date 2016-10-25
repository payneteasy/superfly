package com.payneteasy.superfly.web.security.securehandler;

import java.util.StringTokenizer;

public class Tokenizer {

    private final String text;
    private final StringTokenizer st;

    public Tokenizer(String aText, String aDelim) {
        text = aText;
        st = new StringTokenizer(aText, aDelim);
    }

    public String next(String aName) throws AuthorizationException {
        if(st.hasMoreTokens()) {
            return st.nextToken();
        }

        throw new AuthorizationException("String {} has no token {}", text, aName);
    }
}
