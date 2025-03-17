package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeUserRoleRequest {
    private String username;
    private String newRole;
    private String subsystemHint;
}
