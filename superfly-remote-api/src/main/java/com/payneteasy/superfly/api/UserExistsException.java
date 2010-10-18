package com.payneteasy.superfly.api;

public class UserExistsException extends Exception {
	public UserExistsException(){}
	public UserExistsException(String message){
		super(message);
	}
    public UserExistsException(Throwable cause){
    	super(cause);
    }
    public UserExistsException(String message, Throwable cause){
    	super(message,cause);
    }
}
