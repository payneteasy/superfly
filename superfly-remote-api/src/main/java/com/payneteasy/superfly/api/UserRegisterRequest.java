package com.payneteasy.superfly.api;

/**
 * @author rpuch
 */
public class UserRegisterRequest extends UserDescription {
    private String password;
    private String subsystemHint;
    private RoleGrantSpecification[] roleGrants;
    private HOTPType hotpType;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubsystemHint() {
        return subsystemHint;
    }

    public void setSubsystemHint(String subsystemHint) {
        this.subsystemHint = subsystemHint;
    }

    public RoleGrantSpecification[] getRoleGrants() {
        return roleGrants;
    }

    public void setRoleGrants(RoleGrantSpecification[] roleGrants) {
        this.roleGrants = roleGrants;
    }

    public HOTPType getHotpType() {
        return hotpType;
    }

    public void setHotpType(HOTPType hotpType) {
        this.hotpType = hotpType;
    }
}
