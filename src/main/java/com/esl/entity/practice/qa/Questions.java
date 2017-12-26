package com.esl.entity.practice.qa;

import javax.persistence.*;

@Entity
@Table(name ="practice_questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISC", discriminatorType = DiscriminatorType.STRING)
public abstract class Questions {
	public enum Type {
		MCQuestions, SentenceStructure, QuestionInSentence
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	protected long id;

	// ----------------------------- getter / setter -------------------//

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Type getType() {return null;}

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
		Questions other = (Questions) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Questions(%s) [type=%s]", id, getType());
	}
}
