package com.esl.model;

import java.io.Serializable;
import java.util.Date;

public class PhoneticPracticeQuestionHistory implements Serializable {
	private Long id = null;
	private boolean correct = false;
	private PhoneticQuestion question;
	private PhoneticPracticeHistory history;
	private Date createdDate = new Date();
	
	// ********************** Constructors ********************** //
	public PhoneticPracticeQuestionHistory() {}
	
	public PhoneticPracticeQuestionHistory(boolean correct, PhoneticQuestion question, PhoneticPracticeHistory history) {
		this.correct = correct;
		this.question = question;
		this.history = history;
	}
	
	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }
	
	public Date getCreatedDate() { return createdDate; }
	private void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	
	public boolean isCorrect() { return correct; }
	public void setCorrect(boolean correct) { this.correct = correct; }
	
	public PhoneticQuestion getQuestion() { return question; }
	public void setQuestion(PhoneticQuestion question) {this.question = question; }
	
	public PhoneticPracticeHistory getHistory() { return history; }
	public void setHistory(PhoneticPracticeHistory history) { this.history = history; }
	
	// ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof PhoneticPracticeQuestionHistory)) return false;

		final PhoneticPracticeQuestionHistory history = (PhoneticPracticeQuestionHistory) o;		
		return this.id.equals(history.getId());
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "Phonetic Practice Question History ('" + getId() + "') [ " +
				"is Correct: '" + isCorrect() + "' " +
				"Created Date: '" + getCreatedDate() + "' " +
				" ]";
	}
}
