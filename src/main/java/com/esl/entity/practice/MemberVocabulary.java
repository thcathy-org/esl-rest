package com.esl.entity.practice;

import com.esl.entity.IAuditable;
import com.esl.model.Member;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="member_vocabulary")
public class MemberVocabulary implements Serializable, IAuditable {

	@EmbeddedId
	private MemberVocabularyId id;

	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();

	@Column(name = "LAST_UPDATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedDate;

	@Column(name = "CORRECT")
	private int correct;

	@Column(name = "WRONG")
	private int wrong;

	public MemberVocabulary() {}

	public MemberVocabulary(Member member, String word) {
		this.id = new MemberVocabularyId(member, word);
	}

	public MemberVocabularyId getId() {return id;}
	public MemberVocabulary setId(MemberVocabularyId id) {
		this.id = id;
		return this;
	}

	public int getCorrect() {return correct;}
	public MemberVocabulary setCorrect(int correct) {
		this.correct = correct;
		return this;
	}

	public int getWrong() {return wrong;}
	public MemberVocabulary setWrong(int wrong) {
		this.wrong = wrong;
		return this;
	}

	public Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}

	public Date getLastUpdatedDate() {return lastUpdatedDate;}
	public void setLastUpdatedDate(Date lastUpdatedDate) {this.lastUpdatedDate = lastUpdatedDate;}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MemberVocabulary that = (MemberVocabulary) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("correct", correct)
				.append("wrong", wrong)
				.toString();
	}

	public void updateResult(boolean isCorrect) {
		if (isCorrect) {
			correct++;
		} else {
			wrong++;
		}
	}

	@Embeddable
	static public class MemberVocabularyId implements Serializable {

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name="MEMBER_ID")
		private Member member;

		@Column(name = "WORD")
		private String word;

		public MemberVocabularyId() {}

		public MemberVocabularyId(Member member, String word) {
			this.member = member;
			this.word = word;
		}

		public Member getMember() {	return member;	}
		public MemberVocabularyId setMember(Member member) {
			this.member = member;
			return this;
		}

		public String getWord() {return word;}
		public MemberVocabularyId setWord(String word) {
			this.word = word;
			return this;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("member", member)
					.append("word", word)
					.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MemberVocabularyId that = (MemberVocabularyId) o;
			return Objects.equals(member, that.member) &&
					Objects.equals(word, that.word);
		}

		@Override
		public int hashCode() {
			return Objects.hash(member, word);
		}
	}
}
