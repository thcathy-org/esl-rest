package com.esl.entity.practice.qa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("ENGLISH_QUESTIONS")
public abstract class EnglishQuestions extends Questions {

	@ManyToOne
	@JoinColumn(name="PASSAGE")
	protected Passage passage;

	// ----------------------------- getter / setter -------------------//

	public Passage getPassage() {return passage;}
	public void setPassage(Passage passage) {this.passage = passage;}

	// ---------------------------- Public function -------------------- //

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnglishQuestions other = (EnglishQuestions) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("EnglishQuestions(%s) [type=%s, passageId=%s]", id, getType(), passage!=null?passage.getId():null);
	}
}
