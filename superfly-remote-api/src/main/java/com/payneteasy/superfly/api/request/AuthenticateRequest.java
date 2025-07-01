package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateRequest {
    private String                    username;
    private String                    password;
    private AuthenticationRequestInfo authRequestInfo;

    public AuthenticateRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
