package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PseudoAuthenticateRequest {
    private String username;
    private String subsystemIdentifier;
}
