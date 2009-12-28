package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Represents a result returned from stored routines with no result
 * set returned.
 * 
 * @author Roman Puchkovskiy
 */
public class RoutineResult implements Serializable {
	private static final String OK = "OK";
	
	private String status;
	private String errorMessage;

	@Column(name = "status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "error_message")
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public boolean isOk() {
		return OK.equals(status);
	}
}
