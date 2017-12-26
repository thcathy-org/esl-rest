package com.esl.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IllegalParameterException extends ESLSystemException {
	private static Logger logger = LoggerFactory.getLogger(IllegalParameterException.class);
	
	protected String[] objectNames;
	protected Object[] objects;
	
	public IllegalParameterException() {
		this("illegalParameterException");
	}
	
	public IllegalParameterException(String errorCode) {
		super(errorCode);			
	}
	
	public IllegalParameterException(String[] names, Object[] objects) {
		this("illegalParameterException", names, objects);
	}
	
	public IllegalParameterException(String errorCode, String[] names, Object[] objects) {
		super("illegalParameterException", errorCode);
		for (int i=0; i < names.length; i++) logger.warn("IllegalParameterException: illegal para name[" + names[i] + "] obj[" + objects[i] + "]");
		this.objectNames = names;
		this.objects = objects;
	}

	public String[] getObjectNames() {return objectNames;}
	public void setObjectNames(String[] objectNames) {this.objectNames = objectNames;}

	public Object[] getObjects() {return objects;}
	public void setObjects(Object[] objects) {this.objects = objects;}
	
}
