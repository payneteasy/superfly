package com.payneteasy.superfly.web.security.securehandler;

public class OAuth2AuthorizationParser implements IAuthorizationParser {

    @Override
    public AuthorizationBearer parse(String aText) throws AuthorizationException {
        Tokenizer st = new Tokenizer(aText, " /:");

        String scheme = st.next("scheme");
        if(!"Bearer".equals(scheme)) {
            throw new AuthorizationException("Only Bearer scheme supported, but was '{}'", scheme);
        }

        return new AuthorizationBearer(st.next("subsystem"), st.next("token"));
    }
}
