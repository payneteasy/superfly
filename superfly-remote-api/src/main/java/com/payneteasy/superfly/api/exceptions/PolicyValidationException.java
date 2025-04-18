package com.payneteasy.superfly.api.exceptions;

import lombok.Getter;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 16:19:38
 * (C) 2010
 * Skype: kuccyp
 * @since 1.2
 */
@Getter
public class PolicyValidationException extends SsoClientException {

    public final  static String EMPTY_PASSWORD="P000";
    public final  static String SHORT_PASSWORD="P001";
    public final  static String SIMPLE_PASSWORD="P002";
    public final  static String EXISTING_PASSWORD="P003";

    private final String code;

    public PolicyValidationException(String code) {
        super(400, code);
        this.code = code;
    }

}
