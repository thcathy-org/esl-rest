package com.esl.enumeration;


public enum VerbToBe {
	am,
	are,
	is,
	was,
	were,
	be,
	being,
	been;

	static public String VERB_TO_BE_REGEX;

	static {
		StringBuffer sb = new StringBuffer();
		for (VerbToBe p : VerbToBe.values()) {
			sb.append("\\b").append(p).append("\\b").append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		VERB_TO_BE_REGEX = sb.toString();
	}
}