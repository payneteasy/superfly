package com.payneteasy.superfly.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeTempPasswordRequest {
    private String username;
    private String newPassword;
}
