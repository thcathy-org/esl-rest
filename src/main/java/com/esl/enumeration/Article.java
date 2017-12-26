package com.esl.enumeration;


public enum Article {
	a, an, the;

	static public String ARTICLE_REGEX;

	static {
		StringBuffer sb = new StringBuffer();
		for (Article p : Article.values()) {
			sb.append("\\b").append(p).append("\\b").append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		ARTICLE_REGEX = sb.toString();
	}
}