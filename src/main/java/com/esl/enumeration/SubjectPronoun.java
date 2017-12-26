package com.esl.enumeration;


public enum SubjectPronoun {
	me,
	we,
	us,
	you,
	he,
	him,
	they,
	them,
	she,
	it,
	her,
	my,
	your,
	his,
	its,
	our,
	their,
	mine,
	yours,
	ours,
	theirs,
	i;

	static public String SUBJECT_PRONOUN_REGEX;

	static {
		StringBuffer sb = new StringBuffer();
		for (SubjectPronoun p : SubjectPronoun.values()) {
			sb.append("\\b").append(p).append("\\b").append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		SUBJECT_PRONOUN_REGEX = sb.toString();
	}
}