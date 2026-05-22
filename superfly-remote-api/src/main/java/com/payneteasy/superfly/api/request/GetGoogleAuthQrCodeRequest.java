package com.payneteasy.superfly.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGoogleAuthQrCodeRequest implements Serializable {
    private String secretKey;
    private String issuer;
    private String accountName;
}
