package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.impl.remote.check.KeyPairData;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthEncryptionAlgorithm;

/**
 * Service for subsystems.
 *
 * @author Roman Puchkovskiy
 */
public interface SubsystemService {
    /**
     * Returns a list of all subsystems for a list in the UI.
     *
     * @return subsystems
     */
    List<UISubsystemForList> getSubsystems();

    /**
     * Creates a subsystem.
     *
     * @param subsystem
     *            subsystem to create
     */
    RoutineResult createSubsystem(UISubsystem subsystem);

    /**
     * Updates a subsystem.
     *
     * @param subsystem
     *            subsystem to update
     */
    RoutineResult updateSubsystem(UISubsystem subsystem);

    /**
     * Deletes a subsystem.
     *
     * @param subsystemId
     *            ID of a subsystem to delete
     */
    RoutineResult deleteSubsystem(Long subsystemId);

    /**
     * Returns a list of all subsystems for a subsystem-based filter.
     *
     * @return subsystems
     */
    List<UISubsystemForFilter> getSubsystemsForFilter();

    /**
     * Returns a subsystem by its ID.
     *
     * @param subsystemId    ID of the subsystem
     * @return subsystem or null if no such subsystem
     */
    UISubsystem getSubsystem(long subsystemId);

    /**
     * Returns a subsystem by its name.
     *
     * @param subsystemName    name of the subsystem
     * @return subsystem or null if no such subsystem
     */
    UISubsystem getSubsystemByName(String subsystemName);

    /**
     * Returns the data subsystem authentication needs, including the subsystem token.
     * The UI lookups ({@link #getSubsystem}, {@link #getSubsystemByName}) never return it.
     *
     * @param subsystemName    name of the subsystem
     * @return auth data or null if no such subsystem
     */
    SubsystemAuth getSubsystemAuth(String subsystemName);

    /**
     * Returns the decrypted private key of a subsystem; only remote auth should call it,
     * and must not keep the result longer than one request.
     *
     * @param subsystemName    name of the subsystem
     * @return PEM key or null if the subsystem has none
     * @throws IllegalStateException if the stored key cannot be decrypted
     */
    String getSubsystemPrivateKey(String subsystemName);

    /**
     * Tries to obtain a subsystem token. If user identified
     * by that SSO session can login to the requested subsystem,
     * subsystem token is returned.
     * If user cannot login to that subsystem, null is returned
     *
     * @param ssoSessionId          ID of SSO session
     * @param subsystemIdentifier   name of the subsystem
     * @return subsystem token or null
     */
    SubsystemTokenData issueSubsystemTokenIfCanLogin(long ssoSessionId, String subsystemIdentifier);

    /**
     * Generates a new main token and puts its hash into the subsystem; the hash is persisted by
     * create/update. The raw token is returned for a one-time display and is not stored anywhere.
     *
     * @return raw main token for the subsystem
     */
    String generateMainSubsystemToken(UISubsystem subsystem);

    /**
     *
     * @return key pair data for subsystem
     */
    KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm);
}
