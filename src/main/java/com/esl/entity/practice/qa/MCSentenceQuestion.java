package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@DiscriminatorValue("MC_SENTENSE_QUESTION")
public class MCSentenceQuestion extends SentenceQuestion implements Serializable {

	private static final long serialVersionUID = -5837822746044382925L;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "QUESTION")
	private MCQuestion question;


	// ********************** Constructors ********************** //
	public MCSentenceQuestion() {
		this.setAnswerType(AnswerType.MC);
	}

	public MCSentenceQuestion(Sentence sentence, MCQuestion question) {
		super(sentence);
		this.setAnswerType(AnswerType.MC);
		this.question = question;
	}

	// ********************** Accessor Methods ********************** //
	public MCQuestion getQuestion() {return question;}
	public void setQuestion(MCQuestion question) {this.question = question;}

	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return String.format("MCSentenseQuestion (%s) [startPosition=%s, endPosition=%s]", getId(), getStartPosition(), getEndPosition());
	}
}
