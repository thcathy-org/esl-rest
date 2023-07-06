package com.esl.entity.practice;

import com.esl.entity.IAuditable;
import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name="member_practice_score_card")
public class MemberPracticeScoreCard implements Serializable, IAuditable {
	private static final long serialVersionUID = 3163050468560114821L;

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

	@ManyToOne
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Column(name = "SCORE_CARD_DATE")
	private Date scoreCardDate;

	@Column(name = "SCORE")
	private int score;

	@Column(name = "PRACTICE_TYPE")
	@Enumerated(EnumType.STRING)
	private ESLPracticeType practiceType;

	@Column(name = "LAST_MONTH_STANDING")
	private int lastMonthStanding;

	// ********************** Constructors ********************** //
	public MemberPracticeScoreCard() {}

	public MemberPracticeScoreCard(Member member, Date scoreCardDate, ESLPracticeType practiceType) {
		super();
		this.member = member;
		this.scoreCardDate = scoreCardDate;
		this.practiceType = practiceType;
	}


	// ********************** Accessor Methods ********************** //

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public Date getScoreCardDate() {return scoreCardDate;}
	public void setScoreCardDate(Date scoreCardDate) {this.scoreCardDate = scoreCardDate;}

	public int getScore() {return score;}
	public void setScore(int score) {this.score = score;}
	public void addScore(int score) {this.score += score; }

	public ESLPracticeType getPracticeType() {return practiceType;}
	public void setPracticeType(ESLPracticeType practiceType) {this.practiceType = practiceType;}

	public int getLastMonthStanding() {return lastMonthStanding;}
	public void setLastMonthStanding(int lastMonthStanding) {this.lastMonthStanding = lastMonthStanding;}

	public java.util.Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(java.util.Date createdDate) {this.createdDate = createdDate;}

	public java.util.Date getLastUpdatedDate() {return lastUpdatedDate;}
	public void setLastUpdatedDate(java.util.Date lastUpdatedDate) {this.lastUpdatedDate = lastUpdatedDate;}


	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return String.format("MemberPracticeScoreCard (%s) [userId=%s, scoreCardDate=%s, practiceType=%s, score=%s]", getId(), member.getUserId(),scoreCardDate, practiceType, score);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		result = prime * result + ((practiceType == null) ? 0 : practiceType.hashCode());
		result = prime * result + ((scoreCardDate == null) ? 0 : scoreCardDate.hashCode());
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
		MemberPracticeScoreCard other = (MemberPracticeScoreCard) obj;
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
		if (scoreCardDate == null) {
			if (other.scoreCardDate != null)
				return false;
		} else if (!scoreCardDate.equals(other.scoreCardDate))
			return false;
		return true;
	}
}
