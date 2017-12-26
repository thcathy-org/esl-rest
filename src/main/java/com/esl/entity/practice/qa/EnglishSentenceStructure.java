package com.esl.entity.practice.qa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("SENTENCE_STRUCT")
public class EnglishSentenceStructure extends EnglishQuestions {

	// ----------------------------- getter / setter -------------------//

	@Override
	public Type getType() {return Type.SentenceStructure;}

	// ---------------------------- Public function -------------------- //

}
