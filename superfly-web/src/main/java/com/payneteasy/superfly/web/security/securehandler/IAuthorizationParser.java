package com.payneteasy.superfly.web.security.securehandler;

public interface IAuthorizationParser {

    AuthorizationBearer parse(String aText) throws AuthorizationException;

}
