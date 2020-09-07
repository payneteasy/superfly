package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Used to obtain info about a user actions with no regard to roles or
 * preferences.
 * 
 * @author Roman Puchkovskiy
 * @since 1.1
 */
public class SSOUserWithActions implements Serializable {
    private static final long serialVersionUID = -4340261492793178835L;

    private String name;
    private String email;
    private SSOAction[] actions;

    /**
     * Constructs a user.
     *
     * @param name        user name
     * @param email        email
     * @param actions    user actions
     */
    public SSOUserWithActions(String name, String email, SSOAction[] actions) {
        super();
        this.name = name;
        this.email = email;
        this.actions = actions;
    }

    /**
     * Returns user name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets user name.
     *
     * @param name    name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns user email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets user email.
     *
     * @param email    email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns user's actions.
     *
     * @return actions
     */
    public SSOAction[] getActions() {
        return actions;
    }

    /**
     * Sets user's actions.
     *
     * @param actions    actions to set
     */
    public void setActions(SSOAction[] actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return name;
    }
}
