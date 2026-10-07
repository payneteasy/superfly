package com.payneteasy.superfly.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Result of a one-time password check.
 * <p>
 * Detailed statuses are meant to be shown only after the password step has succeeded; an unknown user or a
 * user not accessible to the calling subsystem always gets {@link Status#INVALID}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOtpResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Status status;

    public enum Status {
        /** The code is valid; its time step is now used. */
        SUCCESS,
        /** Malformed or wrong code, or an unknown user. */
        INVALID,
        /** The code (or a later one) has already been used. */
        ALREADY_USED,
        /** The code belongs to a time step close to, but outside of, the accepted window: device clock is off. */
        CLOCK_SKEW,
        /** The account is locked; the code was not checked. Only an administrator unlocks it. */
        LOCKED
    }
}
