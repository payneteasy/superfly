package com.payneteasy.superfly.service.impl.remote;


/**
 * Used to somehow obtain a subsystem identifier.
 * 
 * @author Roman Puchkovskiy
 */
public interface SubsystemIdentifierObtainer {
    /**
     * Obtains a subsystem identifier.
     *
     * @param systemIdentifier    hint which possibly may be used as identifier
     * @return subsystem identifier
     */
    String obtainSubsystemIdentifier(String systemIdentifier);
}
