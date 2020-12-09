package com.esl.entity.practice;

import com.esl.entity.IAuditable;
import com.esl.enumeration.ESLPracticeType;
import com.esl.enumeration.Medal;
import com.esl.model.Member;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;

@Entity
@Table(name="practice_medal")
public class PracticeMedal implements Serializable, IAuditable {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date createdDate = new java.util.Date();

	@Column(name = "LAST_UPDATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date lastUpdatedDate;

	@ManyToOne(fetch= FetchType.EAGER)
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Column(name = "AWARDED_DATE")
	private java.sql.Date awardedDate;

	@Column(name = "SCORE")
	private int score;

	@Column(name = "PRACTICE_TYPE")
	@Enumerated(EnumType.STRING)
	private ESLPracticeType practiceType;

	@Column(name = "MEDAL")
	@Enumerated(EnumType.STRING)
	private Medal medal;

	// ********************** Constructors ********************** //
	public PracticeMedal() {}


	// ********************** Accessor Methods ********************** //

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public int getScore() {return score;}
	public void setScore(int score) {this.score = score;}

	public ESLPracticeType getPracticeType() {return practiceType;}
	public void setPracticeType(ESLPracticeType practiceType) {this.practiceType = practiceType;}

	public java.sql.Date getAwardedDate() {return awardedDate;}
	public void setAwardedDate(java.sql.Date awardedDate) {this.awardedDate = awardedDate;}

	public Medal getMedal() {return medal;}
	public void setMedal(Medal medal) {	this.medal = medal;}

	public java.util.Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(java.util.Date createdDate) {this.createdDate = createdDate;}

	public java.util.Date getLastUpdatedDate() {return lastUpdatedDate;}
	public void setLastUpdatedDate(java.util.Date lastUpdatedDate) {this.lastUpdatedDate = lastUpdatedDate;}


	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return String.format("PracticeMedal (%s) [userId=%s, medal=%s, awardedDate=%s, practiceType=%s, score=%s]", getId(), member.getUserId(), medal, awardedDate, practiceType, score);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((awardedDate == null) ? 0 : awardedDate.hashCode());
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		result = prime * result + ((practiceType == null) ? 0 : practiceType.hashCode());
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
		PracticeMedal other = (PracticeMedal) obj;
		if (awardedDate == null) {
			if (other.awardedDate != null)
				return false;
		} else if (!awardedDate.equals(other.awardedDate))
			return false;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		if (practiceType == null) {
			if (other.practiceType != null)
				return false;
		} else if (!practiceType.equals(other.practiceType))
			return false;
		return true;
	}

	// ********************** Comparator ********************** //
	public static class TopMedalComparator implements Comparator<PracticeMedal> {
		@Override
		public int compare(PracticeMedal o1, PracticeMedal o2) {
			return o1.getMedal().weight - o2.getMedal().weight;
		}
	}


}
