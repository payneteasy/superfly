package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.SSOUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CheckOtpRequest {
    private SSOUser user;
    private String  code;
}
