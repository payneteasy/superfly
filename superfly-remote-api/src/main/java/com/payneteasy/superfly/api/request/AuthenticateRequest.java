package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticateRequest {
    private String                    username;
    private String                    password;
    private AuthenticationRequestInfo authRequestInfo;
}
