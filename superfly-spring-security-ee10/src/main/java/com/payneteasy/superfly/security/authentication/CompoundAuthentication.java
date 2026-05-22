package com.payneteasy.superfly.security.authentication;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;

/**
 * Holds several authentication results and an authentication request.
 *
 * @author Roman Puchkovskiy
 */
public class CompoundAuthentication extends EmptyAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 8781927069834292831L;

    private       List<Authentication> readyAuthentications = new ArrayList<Authentication>();
    private final Authentication       currentAuthenticationRequest;

    public CompoundAuthentication() {
        this(null);
    }

    public CompoundAuthentication(Authentication request) {
        this(new Authentication[]{}, request);
    }

    public CompoundAuthentication(Authentication[] readyAuthentications,
            Authentication currentAuthenticationRequest) {
        super();
        this.readyAuthentications = new ArrayList<>(Arrays.asList(readyAuthentications));
        this.currentAuthenticationRequest = currentAuthenticationRequest;
    }

    public void addReadyAuthentication(Authentication auth) {
        readyAuthentications.add(auth);
    }

    public Authentication getCurrentAuthenticationRequest() {
        return currentAuthenticationRequest;
    }

    public Authentication[] getReadyAuthentications() {
        return readyAuthentications.toArray(new Authentication[readyAuthentications.size()]);
    }

    public Authentication getLatestReadyAuthentication() {
        if (readyAuthentications.isEmpty()) {
            return null;
        }
        return readyAuthentications.get(readyAuthentications.size() - 1);
    }

    public Authentication getFirstReadyAuthentication() {
        if (readyAuthentications.isEmpty()) {
            return null;
        }
        return readyAuthentications.get(0);
    }

}
