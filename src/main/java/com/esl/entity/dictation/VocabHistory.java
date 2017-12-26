package com.esl.entity.dictation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "dictation_vocab_history")
public class VocabHistory implements Serializable {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "TOTAL_CORRECT")
	private int totalCorrect;

	@Column(name = "TOTAL_WRONG")
	private int totalWrong;

	@ManyToOne()
	@JoinColumn(name="VOCAB_ID")
	private Vocab vocab;

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name="MEMBER_DICTATION_HISTORY_ID")
	private MemberDictationHistory dictationHistory;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public VocabHistory() {
		totalWrong = 0;
		totalCorrect = 0;
		createdDate = new Date();
	}

	public VocabHistory(Vocab vocab) {
		this();
		this.vocab = vocab;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public int getTotalCorrect() {	return totalCorrect;}
	public void setTotalCorrect(int totalCorrect) {	this.totalCorrect = totalCorrect;}

	public int getTotalWrong() {return totalWrong;}
	public void setTotalWrong(int totalWrong) {	this.totalWrong = totalWrong;}

	public Vocab getVocab() {return vocab;}
	public void setVocab(Vocab vocab) {this.vocab = vocab;}

	public MemberDictationHistory getDictationHistory() {	return dictationHistory;}
	public void setDictationHistory(MemberDictationHistory dictationHistory) {this.dictationHistory = dictationHistory;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public int getCorrectPercent() {
		if (totalCorrect + totalWrong == 0) return 0;
		return totalCorrect * 100 / (totalCorrect + totalWrong);
	}
	public int getWrongPercent() {
		if (totalCorrect + totalWrong == 0) return 0;
		return totalWrong * 100 / (totalCorrect + totalWrong);
	}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof VocabHistory)) return false;

		final VocabHistory v = (VocabHistory) o;
		return this.id.equals(v.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Vocab History ("); sb.append(getId()); sb.append(")");
		return  sb.toString();
	}
}
