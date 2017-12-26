package com.esl.entity.practice;

import com.esl.enumeration.VerbToBe;

import java.util.List;

public class VerbToBePractice extends GrammarPractice {
	private static final long serialVersionUID = 1L;

	private List<String> verbToBe;

	@Override public List<String> getQuestions() {return verbToBe;}
	@Override public void setQuestions(List<String> verbToBe) {this.verbToBe = verbToBe;}

	@Override public String getQuestionsRegEx() {return VerbToBe.VERB_TO_BE_REGEX; }
	@Override public Object[] getQuestionsString() { return VerbToBe.values(); }

	public List<String> getVerbToBe() {	return verbToBe;}
	public void setVerbToBe(List<String> verbToBe) {this.verbToBe = verbToBe;}

}
