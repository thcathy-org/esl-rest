package com.esl.entity.dictation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "dictation_vocab")
public class Vocab implements Serializable {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "WORD")
	private String word;

	@Column(name = "TOTAL_CORRECT")
	private int totalCorrect;

	@Column(name = "TOTAL_WRONG")
	private int totalWrong;

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name="DICTATION_ID")
	@JsonIgnore
	private Dictation dictation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public Vocab() {
		totalWrong = 0;
		totalCorrect = 0;
		createdDate = new Date();
	}

	public Vocab(String word) {
		this();
		this.word = word;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public String getWord() {return word;}
	public void setWord(String word) {this.word = word;}

	public int getTotalCorrect() {	return totalCorrect;}
	public void setTotalCorrect(int totalCorrect) {	this.totalCorrect = totalCorrect;}

	public int getTotalWrong() {return totalWrong;}
	public void setTotalWrong(int totalWrong) {	this.totalWrong = totalWrong;}

	public Dictation getDictation() {return dictation;}
	public void setDictation(Dictation dictation) {	this.dictation = dictation;	}

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
		if (!(o instanceof Vocab)) return false;

		final Vocab v = (Vocab) o;
		return this.id.equals(v.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Vocab ("); sb.append(getId()); sb.append("): ");
		sb.append("Word["); sb.append(getWord()); sb.append("] ");
		return  sb.toString();
	}
}
