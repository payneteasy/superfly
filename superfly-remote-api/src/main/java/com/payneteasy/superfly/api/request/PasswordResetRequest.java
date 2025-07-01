package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author rpuch
 */
@Data
@Builder
public class PasswordResetRequest implements Serializable {
    private String username;
    private String password;
    private boolean sendByEmail;
}
