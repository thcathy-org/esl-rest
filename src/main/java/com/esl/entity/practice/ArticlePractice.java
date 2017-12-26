package com.esl.entity.practice;

import com.esl.enumeration.Article;

import java.util.List;

public class ArticlePractice extends GrammarPractice {
	private static final long serialVersionUID = 1L;

	private List<String> articles;

	@Override public List<String> getQuestions() {return articles;}
	@Override public void setQuestions(List<String> articles) {this.articles = articles;}

	@Override public String getQuestionsRegEx() {return Article.ARTICLE_REGEX; }

	@Override public Object[] getQuestionsString() { return Article.values(); }

	public List<String> getArticles() {return articles;}
	public void setArticles(List<String> articles) {this.articles = articles;}

}
