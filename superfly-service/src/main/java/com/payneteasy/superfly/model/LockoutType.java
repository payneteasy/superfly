package com.payneteasy.superfly.model;

/**
 * Type of the lockout.
 *
 * @author Roman Puchkovskiy
 */
public enum LockoutType {
    SESSION, ROLES, PASSWORD, HOTP;
}
