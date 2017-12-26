package com.esl.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class MemberWord implements Serializable {
	public enum Ordering {	
		DateAsc, DateDesc, TrialCountAsc, TrialCountDesc, CorrectCountAsc, CorrectCountDesc;
	}
	
	private Long id = null;
	private Member member;
	private PhoneticQuestion word;
	private int trialCount = 0;
	private int correctCount = 0;
	private Date createdDate = new Date();
	
	// ********************** Constructors ********************** //
	public MemberWord() {}
	
	public MemberWord(Member member, PhoneticQuestion word) {
		this.member = member;
		this.word = word;
	}
	
	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public int getCorrectCount() {return correctCount;}
	public void setCorrectCount(int correctCount) {this.correctCount = correctCount;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public int getTrialCount() {return trialCount;}
	public void setTrialCount(int trialCount) {	this.trialCount = trialCount;}

	public PhoneticQuestion getWord() {return word;}
	public void setWord(PhoneticQuestion word) {this.word = word;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	
	public void addTrial(int mark) {
		trialCount++;
		correctCount+=mark;
	}
	
	// ********************** Comparator ********************** //
	public static class DateComparator implements Comparator<MemberWord> {
		@Override
		public int compare(MemberWord o1, MemberWord o2) {			
			return o2.createdDate.compareTo(o1.createdDate);
		}		
	}
	
	public static class TrialCountComparator implements Comparator<MemberWord> {
		@Override
		public int compare(MemberWord o1, MemberWord o2) {
			return o2.getTrialCount() - o1.getTrialCount();
		}
	}
	
	public static class CorrectCountComparator implements Comparator<MemberWord> {
		@Override
		public int compare(MemberWord o1, MemberWord o2) {
			return o2.getCorrectCount() - o1.getCorrectCount();
		}
	}
	
	// ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof MemberWord)) return false;

		final MemberWord w = (MemberWord) o;		
		return this.id.equals(w.getId());
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "Member-Word (" + getId() + "): " +
				"Member[" + getMember().getUserId() + "]" +
				"Word[" + getWord().getWord() + "]";				
	}
	
	
	
}
