package com.esl.entity.dictation;

import java.util.List;

public class SentenceHistory {
	public final String question;
	public final String answer;
	public final List<String> questionSegments;
	public final List<Boolean> isCorrect;

	public String getQuestion() {return question;	}
	public String getAnswer() {	return answer;}
	public List<String> getQuestionSegments() { return questionSegments;}
	public List<Boolean> getIsCorrect() {return isCorrect;}

	public SentenceHistory(String question, String answer, List<String> questionSegments, List<Boolean> isCorrect) {
		this.question = question;
		this.answer = answer;
		this.questionSegments = questionSegments;
		this.isCorrect = isCorrect;
	}

}
