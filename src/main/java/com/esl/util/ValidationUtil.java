package com.esl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValidationUtil {
	private static Logger log = LoggerFactory.getLogger(ValidationUtil.class);
	
	public static boolean isEmailAddress(final String aEmailAddress)
	{
		if (aEmailAddress == null) return false;
		
		String[] tokens = aEmailAddress.split("@");
	    return tokens.length == 2 && !tokens[0].equals("") && !tokens[1].equals("") ;
	}
	
	public static boolean isValidDate(final String date)
	{	  
	   SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");	   
	   Date testDate = null;

	   try {
	     testDate = sdf.parse(date);
	   }
	   catch (ParseException e) {	     
	     return false;
	   }

	   if (!sdf.format(testDate).equals(date)) {	     
	     return false;
	   }
	   return true;
	}
	
	public static boolean isAlphaNumeric(final String s) {
		if (s == null || s.length() < 1) return false;
		
		if (!s.matches("[a-zA-Z0-9]*")) return false;
			
		return true;
	}
	
	public static boolean isMatch(final String s, final String regExp) {
		if (s == null || s.length() < 1) return false;		
		if (!s.matches(regExp)) return false;
			
		return true;
	}
	
	public static boolean isContainInvalidCharacters(final String s) {
		if (s == null || s.length() < 1) return false;
		
		if (!s.matches("[^'<>\"]*")) return true;
			
		return false;
	}
	
	/**
	 * Test a string contain alpha, /, -, . or ' ' only
	 * @return
	 */
	public static boolean isValidWord(final String s) {
		log.debug("isValidWord [{}]",s);
		if (s == null || s.length() < 1) {
			log.debug("isValidWord s is emply");
			 return false;
		}
		else if (!s.matches("([a-zA-Z \\-']+)?")) {
			log.debug("isValidWord  not match [{}]",s);
			return false;
		}
		else {
			return true;
		}
	}

	public static boolean wordEqual(String input1, String input2) {
		return input1.replaceAll(" ", "").replaceAll("-", "").toLowerCase()
				.equals(input2.replaceAll(" ", "").replaceAll("-", "").toLowerCase());
	}

	public static String alphabetOnly(String input) {
		return input.replaceAll("[^A-Za-z]+", "");
	}

}
