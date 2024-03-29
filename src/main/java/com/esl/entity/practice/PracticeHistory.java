package com.esl.entity.practice;

import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="PRACTICE_HISTORY")
public class PracticeHistory implements Serializable {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Column(name = "WRONG")
	private int wrong;

	@Column(name = "CORRECT")
	private int correct;

	@Column(name = "PERCENTAGE")
	private double percentage;

	@Column(name = "HISTORY_JSON")
	private String historyJSON;

	@Column(name = "ESL_PRACTICE_TYPE")
	private ESLPracticeType eslPracticeType;

	@Column(name = "DICTATION_ID")
	private long dictationId;

	// ********************** Constructors ********************** //
	public PracticeHistory() {}

	// ********************** Accessor Methods ********************** //

	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public Member getMember() {return member;}
	public PracticeHistory setMember(Member member) {
		this.member = member;
		return this;
	}

	public Date getCreatedDate() {return createdDate;}
	public PracticeHistory setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		return this;
	}

	public int getWrong() {	return wrong;}
	public PracticeHistory setWrong(int wrong) {
		this.wrong = wrong;
		return this;
	}

	public int getCorrect() {return correct;}
	public PracticeHistory setCorrect(int correct) {
		this.correct = correct;
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

	public ESLPracticeType getEslPracticeType() {return eslPracticeType;}
	public PracticeHistory setEslPracticeType(ESLPracticeType eslPracticeType) {
		this.eslPracticeType = eslPracticeType;
		return this;
	}

	public long getDictationId() {return dictationId;}
	public PracticeHistory setDictationId(long dictationId) {
		this.dictationId = dictationId;
		return this;
	}

	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("createdDate", createdDate)
				.append("member", member)
				.append("correct", correct)
				.append("wrong", wrong)
				.append("percentage", percentage)
				.append("eslPracticeType", eslPracticeType)
				.toString();
	}
}
