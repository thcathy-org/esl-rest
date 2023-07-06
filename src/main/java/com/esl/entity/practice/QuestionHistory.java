package com.esl.entity.practice;

import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "QUESTION_HISTORY")
public class QuestionHistory implements Serializable {
	static DecimalFormat percentageFormat = new DecimalFormat("#.##");

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "TOTAL_CORRECT")
	private int totalCorrect;

	@Column(name = "TOTAL_ATTEMPT")
	private int totalAttempt;

	@Column(name = "WORD")
	private String word;

	@Column(name = "PRACTICE_TYPE")
	@Enumerated(EnumType.STRING)
	private ESLPracticeType practiceType;

	@ManyToOne(fetch= FetchType.EAGER)
	@JoinColumn(name="MEMBER_ID")
	private Member member;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public QuestionHistory() {
		createdDate = new Date();
	}

	public QuestionHistory(String word) {
		this();
		this.word = word;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public int getTotalCorrect() {return totalCorrect;}
	public void setTotalCorrect(int totalCorrect) {	this.totalCorrect = totalCorrect;}

	public int getTotalAttempt() {return totalAttempt;}
	public void setTotalAttempt(int totalAttempt) {	this.totalAttempt = totalAttempt;}

	public String getWord() {return word;}
	public void setWord(String word) {this.word = word;	}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public ESLPracticeType getPracticeType() {return practiceType;}
	public void setPracticeType(ESLPracticeType practiceType) {	this.practiceType = practiceType;}

	public double correctPercentage() {
		return Math.round(totalCorrect * 10000 / totalAttempt) / 100;
	}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof QuestionHistory)) return false;

		final QuestionHistory v = (QuestionHistory) o;
		return this.id.equals(v.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("userId", member.getUserId())
				.append("word", word)
				.append("practiceType", practiceType)
				.append("totalCorrect", totalCorrect)
				.append("totalAttempt", totalAttempt)
				.toString();
	}
}
