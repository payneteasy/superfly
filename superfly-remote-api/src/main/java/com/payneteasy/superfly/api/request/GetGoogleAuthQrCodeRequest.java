package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetGoogleAuthQrCodeRequest {
    private String secretKey;
    private String issuer;
    private String accountName;
}
