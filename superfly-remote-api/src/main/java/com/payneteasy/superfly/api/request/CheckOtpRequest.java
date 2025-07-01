package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.SSOUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class CheckOtpRequest {
    private SSOUser user;
    private String  code;
}
