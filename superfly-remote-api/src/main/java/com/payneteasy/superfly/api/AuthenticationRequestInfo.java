package com.payneteasy.superfly.api;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Holds additional information about authentication request.
 *
 * @author Roman Puchkovskiy
 * @since 1.0
 */
@Setter
@Getter
public class AuthenticationRequestInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -4678568813683343136L;

    /**
     * -- GETTER --
     *  Returns IP address of the client who requests authentication. This is
     *  the IP of actual client, not of the subsystem to which he tries to
     *  log in.
     *
     *
     * -- SETTER --
     *  Sets client's IP address.
     *
     @return client IP address
      * @param ipAddress    address to set
      *
     */
    private String ipAddress;
    /**
     * -- GETTER --
     *  Returns some textual session info attached to this authentication
     *  request.
     *
     *
     * -- SETTER --
     *  Sets session info.
     *
     @return session info
      * @param sessionInfo
      *
     */
    private String sessionInfo;
    /**
     * -- GETTER --
     *  Returns an identifier of a subsystem which may be used to identify a
     *  subsystem which made call. It's not mandatory to fill this field as
     *  other means may be used to identify calling subsystem (for instance,
     *  SSL certificates).
     *
     *
     * -- SETTER --
     *  Sets subsystem identifier.
     *
     @return subsystem identifier
      * @param subsystemIdentifier    identifier to set
     */
    private String subsystemIdentifier;

    @Override
    public String toString() {
        return String.format("Auth request for subsystem [%s] from IP [%s], session info [%s]",
                subsystemIdentifier, ipAddress, sessionInfo);
    }
}
