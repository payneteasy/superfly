package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserOtpTypeRequest {
    private String username;
    private String otpType;
}
