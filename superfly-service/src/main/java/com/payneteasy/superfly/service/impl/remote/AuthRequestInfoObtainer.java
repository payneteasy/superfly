package com.payneteasy.superfly.service.impl.remote;


/**
 * Obtainer which obtains subsystem identifier from AuthenticationRequestInfo.
 * For it to work, such info must be put to that object which is not mandatory.
 * 
 * @author Roman Puchkovskiy
 */
public class AuthRequestInfoObtainer implements SubsystemIdentifierObtainer {

    public String obtainSubsystemIdentifier(String hint) {
        return hint;
    }

}
