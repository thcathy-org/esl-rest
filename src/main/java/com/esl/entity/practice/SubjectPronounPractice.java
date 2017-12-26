package com.esl.entity.practice;

import com.esl.enumeration.SubjectPronoun;

import java.util.List;

public class SubjectPronounPractice extends GrammarPractice {
	private static final long serialVersionUID = 1L;

	private List<String> subjectPronouns;

	@Override public List<String> getQuestions() {return subjectPronouns;}
	@Override public void setQuestions(List<String> subjectPronouns) {this.subjectPronouns = subjectPronouns;}

	@Override public String getQuestionsRegEx() {return SubjectPronoun.SUBJECT_PRONOUN_REGEX; }
	@Override public Object[] getQuestionsString() { return SubjectPronoun.values(); }

}
