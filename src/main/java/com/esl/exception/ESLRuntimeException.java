package com.esl.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESLRuntimeException extends RuntimeException {
	private static Logger logger = LoggerFactory.getLogger(ESLRuntimeException.class);
	
	protected String errorCode;
	
	public ESLRuntimeException() {
		super();
		logger.warn("unknown exception created");		
	}
	
	public ESLRuntimeException(String errorCode) {
		this(errorCode, "ESLRuntimeException");
	}
	
	public ESLRuntimeException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
		logger.warn("exception[" + errorCode + "] created");
	}

	public String getErrorCode() {	return errorCode;	}
	public void setErrorCode(String errorCode) {this.errorCode = errorCode;	}
	
}
