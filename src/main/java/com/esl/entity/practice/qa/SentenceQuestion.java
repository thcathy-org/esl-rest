package com.esl.entity.practice.qa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_sentence_question")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISC", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("SENTENSE_QUESTION")
public class SentenceQuestion implements Serializable {
	public enum QuestionType {
		Block, Underline;
	}

	public enum AnswerType {
		Fillin, DragNDrop, MC;
	}

	private static final long serialVersionUID = -5837822746044382925L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SENTENCE")
	private Sentence sentence;

	@Column(name = "START_POS")
	private int startPosition;

	@Column(name = "END_POS")
	private int endPosition;

	@Column(name = "TITLE", length = 1000)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "QUESTION_TYPE", length = 30)
	private QuestionType questionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "ANSWER_TYPE", length = 30)
	private AnswerType answerType;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public SentenceQuestion() {}

	public SentenceQuestion(Sentence sentence) {
		this.sentence = sentence;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Sentence getSentence() {return sentence;}
	public void setSentence(Sentence sentence) {this.sentence = sentence;}

	public int getStartPosition() {return startPosition;}
	public void setStartPosition(int startPosition) {this.startPosition = startPosition;}

	public int getEndPosition() {return endPosition;}
	public void setEndPosition(int endPosition) {this.endPosition = endPosition;}

	public QuestionType getQuestionType() {return questionType;}
	public void setQuestionType(QuestionType questionType) {this.questionType = questionType;}

	public AnswerType getAnswerType() {return answerType;}
	public void setAnswerType(AnswerType answerType) {this.answerType = answerType;}

	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}

	public Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}

	// ********************** Common Methods ********************** //

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SentenceQuestion other = (SentenceQuestion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("SentenseQuestion (%s) [startPosition=%s, endPosition=%s, sentence=%s,  questionType=%s, answerType=%s]", id, startPosition, endPosition, sentence, questionType, answerType);
	}
}
