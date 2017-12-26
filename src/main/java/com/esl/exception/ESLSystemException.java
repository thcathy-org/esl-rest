package com.esl.exception;

public class ESLSystemException extends ESLRuntimeException {
	public ESLSystemException() {
		super();				
	}
	
	public ESLSystemException(String errorCode) {
		this(errorCode, "ESLSystemException");			
	}
		
	public ESLSystemException(String errorCode, String message) {
		super(errorCode, message);		
	}
}
