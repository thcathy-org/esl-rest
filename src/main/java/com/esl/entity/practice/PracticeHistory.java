package com.esl.entity.practice;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;

@Entity
@Table(name="PRACTICE_HISTORY")
public class PracticeHistory implements Serializable {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long id;

	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Column(name = "FULL_MARK")
	private int fullMark;

	@Column(name = "MARK")
	private int mark;

	@Column(name = "PERCENTAGE")
	private double percentage;

	@Column(name = "HISTORY_JSON")
	private String historyJSON;

	@Column(name = "ESL_PRACTICE_TYPE")
	private ESLPracticeType eslPracticeType;

	// ********************** Constructors ********************** //
	public PracticeHistory() {}

	// ********************** Accessor Methods ********************** //

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public Date getCreatedDate() {return createdDate;}
	public PracticeHistory setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		return this;
	}

	public int getFullMark() {return fullMark;}
	public PracticeHistory setFullMark(int fullMark) {
		this.fullMark = fullMark;
		return this;
	}

	public int getMark() {	return mark;}
	public PracticeHistory setMark(int mark) {
		this.mark = mark;
		return this;
	}

	public double getPercentage() {	return percentage;}
	public PracticeHistory setPercentage(double percentage) {
		this.percentage = percentage;
		return this;
	}

	public String getHistoryJSON() {return historyJSON;	}
	public PracticeHistory setHistoryJSON(String historyJSON) {
		this.historyJSON = historyJSON;
		return this;
	}

	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("createdDate", createdDate)
				.append("member", member)
				.append("fullMark", fullMark)
				.append("mark", mark)
				.append("percentage", percentage)
				.append("eslPracticeType", eslPracticeType)
				.toString();
	}
}
