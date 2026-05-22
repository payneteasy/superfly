package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serial;

/**
 * Extension of UsernamePasswordAuthenticationToken which also allows to
 * transfer AuthenticationRequestInfo instance.
 *
 * @author Roman Puchkovskiy
 */
@Getter
public class UsernamePasswordAuthRequestInfoAuthenticationToken extends
        UsernamePasswordAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 9204822041434318662L;

    private final AuthenticationRequestInfo authRequestInfo;
    private       String                    secondFactory;

    public UsernamePasswordAuthRequestInfoAuthenticationToken(Object principal,
                                                              Object credentials,
                                                              AuthenticationRequestInfo authRequestInfo
    ) {
        super(principal, credentials);
        this.authRequestInfo = authRequestInfo;
    }

    public UsernamePasswordAuthRequestInfoAuthenticationToken withSecondFactory(String secondFactory) {
        this.secondFactory = secondFactory;
        return this;
    }

}
