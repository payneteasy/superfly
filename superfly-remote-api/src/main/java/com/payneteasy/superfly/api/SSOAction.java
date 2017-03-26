package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * SSO action (corresponds to a role in JEE security model).
 * 
 * @author Roman Puchkovskiy
 * @since 1.0
 */
public class SSOAction implements Serializable {
    private static final long serialVersionUID = 8596869669651056660L;

    private String name;
    private boolean loggingNeeded;

    /**
     * Constructs action.
     *
     * @param name            action name
     * @param loggingNeeded    whether this action will be logged when used in
     * the subsystem
     */
    public SSOAction(String name, boolean loggingNeeded) {
        super();
        this.name = name;
        this.loggingNeeded = loggingNeeded;
    }

    /**
     * Returns action name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets action name.
     *
     * @param name    name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this action should be logged when used.
     *
     * @return true if logged
     */
    public boolean isLoggingNeeded() {
        return loggingNeeded;
    }

    /**
     * Sets whether this action should be logged when used.
     *
     * @param loggingNeeded    value to set
     */
    public void setLoggingNeeded(boolean loggingNeeded) {
        this.loggingNeeded = loggingNeeded;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SSOAction ssoAction = (SSOAction) o;

        if (name != null ? !name.equals(ssoAction.name) : ssoAction.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
