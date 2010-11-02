package com.payneteasy.superfly.api;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 16:19:38
 * (C) 2010
 * Skype: kuccyp
 * @since 1.2
 */
public class PolicyValidationException extends SSOException {

    public final  static String EMPTY_PASSWORD="P000";
    public final  static String SHORT_PASSWORD="P001";
    public final  static String SIMPLE_PASSWORD="P002";
    public final  static String EXISTING_PASSWORD="P003";

    private final String code;

    public PolicyValidationException(String code) {
    	super(code);
        this.code = code;
    }

    public PolicyValidationException() {
		super();
		code = null;
	}

	public PolicyValidationException(String code, Throwable cause) {
		super(code, cause);
		this.code = code;
	}

	public PolicyValidationException(Throwable cause) {
		super(cause);
		this.code = null;
	}

	public String getCode() {
        return code;
    }
}
