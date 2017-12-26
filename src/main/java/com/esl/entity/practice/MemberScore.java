package com.esl.entity.practice;

import com.esl.entity.IAuditable;
import com.esl.model.Member;

import javax.persistence.*;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="MEMBER_SCORE")
public class MemberScore implements Serializable, IAuditable {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long id;

	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();

	@Column(name = "LAST_UPDATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Column(name = "SCORE_YEAR_MONTH")
	private int scoreYearMonth;

	@Column(name = "SCORE")
	private int score;

	// ********************** Constructors ********************** //
	public MemberScore() {}

	public MemberScore(Member member, int scoreYearMonth) {
		this.member = member;
		this.scoreYearMonth = scoreYearMonth;
	}


	// ********************** Accessor Methods ********************** //

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public int getScoreYearMonth() {return scoreYearMonth;}
	public void setScoreYearMonth(int scoreYearMonth) {this.scoreYearMonth = scoreYearMonth;}

	public int getScore() {return score;}
	public void setScore(int score) {this.score = score;}
	public void addScore(int score) {this.score += score; }

	public Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}

	public Date getLastUpdatedDate() {return lastUpdatedDate;}
	public void setLastUpdatedDate(Date lastUpdatedDate) {this.lastUpdatedDate = lastUpdatedDate;}

	public Date getYearMonthAsDate() throws ParseException {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMM");
		return dateformat.parse(String.valueOf(scoreYearMonth));
	}

	// ********************** Common Methods ********************** //

	public static int thisMonth() {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMM");
		return Integer.parseInt(dateformat.format(new Date()));
	}

	public static int allTimesMonth() {
		return Integer.MAX_VALUE;
	}

	public static int lastSixMonth() {
		return lastMonthBy(6);
	}

	public static int lastMonthBy(int offset) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -offset);
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMM");
		return Integer.parseInt(dateformat.format(c.getTime()));
	}

	@Override
	public String toString() {
		return String.format("MemberScore (%s) [userId=%s, scoreCardDate=%s, score=%s, last update=%s]", getId(), member.getId(),scoreYearMonth, score, lastUpdatedDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		result = prime * result + (scoreYearMonth);
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
		MemberScore other = (MemberScore) obj;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		if (scoreYearMonth != other.scoreYearMonth)
			return false;
		return true;
	}
}
