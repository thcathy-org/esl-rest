package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("MC_QUESTIONS")
public class EnglishMCQuestions extends EnglishQuestions {
	@OneToMany
	@JoinTable(name="eng_mc_questions_map",
			joinColumns=@JoinColumn(name="ENGLISH_QUESTIONS_ID"),
			inverseJoinColumns=@JoinColumn(name="PRACTICE_MC_QUESTION_ID"))
			private List<MCQuestion> questions;

	// ----------------------------- getter / setter -------------------//
	public List<MCQuestion> getQuestions() {	return questions;}
	public void setQuestions(List<MCQuestion> questions) {	this.questions = questions;}

	@Override
	public Type getType() {return Type.MCQuestions;}

	// ---------------------------- Public function -------------------- //

}
