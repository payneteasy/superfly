package com.payneteasy.superfly.model.ui.subsystem;

import javax.persistence.Column;

/**
 * A subsystem as ui_get_subsystem returns it. The flag is not part of {@link UISubsystem}: that class is also the
 * parameter object of the create and update procedures, which have no such parameter.
 */
public class UISubsystemView extends UISubsystem {
    private boolean subsystemTokenSet;

    @Column(name = "subsystem_token_set")
    public boolean isSubsystemTokenSet() {
        return subsystemTokenSet;
    }

    public void setSubsystemTokenSet(boolean subsystemTokenSet) {
        this.subsystemTokenSet = subsystemTokenSet;
    }
}
