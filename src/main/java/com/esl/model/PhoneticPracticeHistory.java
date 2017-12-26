package com.esl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhoneticPracticeHistory implements Serializable {
	private Long id = null;
	private int mark = 0;
	private int fullMark = 0;
	private Date startedTime = new Date();
	private Date completedTime = new Date();
	private Member member;
	private Grade grade;
	private List questionHistories = new ArrayList();
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public PhoneticPracticeHistory() {}
	
	public PhoneticPracticeHistory(Date startedTime, Member member, int mark, List questionHistories) {
		this.startedTime = startedTime;
		this.member = member;
		this.mark = mark;
		this.questionHistories = questionHistories;
	}
	
	public PhoneticPracticeHistory(Member member) {
		this.member = member;
		if (member != null) member.addPhoneticPractices(this);
	}
	
	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	
	public int getMark() { return mark; }
	public void setMark(int mark) { this.mark = mark; }
	
	public int getFullMark() {	return fullMark;}
	public void setFullMark(int fullMark) {	this.fullMark = fullMark;}

	public Date getStartedTime() { return startedTime; }
	public void setStartedTime(Date startedTime) { this.startedTime = startedTime; }
	
	public Date getCompletedTime() { return completedTime; }
	public void setCompletedTime(Date completedTime) { this.completedTime = completedTime; }
	
	public Member getMember() { return member; }
	public void setMember(Member member) { this.member = member; }
	
	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {this.grade = grade;}

	public List getQuestionHistories() { return questionHistories; }
	public void setQuestionHistories(List questionHistories) { this.questionHistories = questionHistories; }
	public void addQuestionHistories(PhoneticPracticeQuestionHistory questionHistory) {
		if (questionHistory == null) throw new IllegalArgumentException("Can't add a null question history.");
		questionHistory.setHistory(this);
		this.questionHistories.add(questionHistory);
	}
	
	// ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof PhoneticPracticeHistory)) return false;

		final PhoneticPracticeHistory history = (PhoneticPracticeHistory) o;		
		return this.id.equals(history.getId());
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "Phonetic Practice History ('" + getId() + "') [ " +
				"Start Time: '" + getStartedTime() + "' " +
				"Total Question: '" + getQuestionHistories().size() + "' " +
				"Mark: '" + getMark() + "' " +
				" ]";
	}
}
