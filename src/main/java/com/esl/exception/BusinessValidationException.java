package com.esl.exception;

public class BusinessValidationException extends ESLRuntimeException {
	public static String TOO_MANY_VOCABS = "tooManyVocabs";
	public static String NO_VOCAB_SET = "noVocabSet";
	
	public BusinessValidationException() {
		super();				
	}
		
	public BusinessValidationException(String message) {
		this("businessValidationException", message);			
	}
		
	public BusinessValidationException(String errorCode, String message) {
		super(errorCode, message);
	}
}
