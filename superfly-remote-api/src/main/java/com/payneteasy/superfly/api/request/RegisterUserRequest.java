package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.RoleGrantSpecification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserRequest {
    private String                 username;
    private String                 password;
    private String                 email;
    private String                 subsystemIdentifier;
    private RoleGrantSpecification roleSpec;
    private String                 publicKey;
}
