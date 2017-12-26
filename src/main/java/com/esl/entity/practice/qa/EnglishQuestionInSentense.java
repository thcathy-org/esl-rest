package com.esl.entity.practice.qa;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("Q_IN_SENTENCE")
public class EnglishQuestionInSentense extends EnglishQuestions {
	@OneToMany
	@JoinTable(name="eng_q_in_sentence_questions_map",
			joinColumns=@JoinColumn(name="ENGLISH_QUESTIONS_ID"),
			inverseJoinColumns=@JoinColumn(name="PRACTICE_SENTENCE_QUESTION_ID"))
			private List<SentenceQuestion> questions;

	// ----------------------------- getter / setter -------------------//
	public List<SentenceQuestion> getQuestions() {	return questions;}
	public void setQuestions(List<SentenceQuestion> questions) {	this.questions = questions;}

	@Override
	public Type getType() {return Type.QuestionInSentence;}

	// ---------------------------- Public function -------------------- //

}
