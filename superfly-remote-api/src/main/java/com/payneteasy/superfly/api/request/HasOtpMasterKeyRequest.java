package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HasOtpMasterKeyRequest {
    private String username;
}
