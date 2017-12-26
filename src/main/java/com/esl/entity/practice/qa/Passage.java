package com.esl.entity.practice.qa;

import com.esl.model.Member;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_passage")
public class Passage implements Serializable {
	public enum DisplayType {
		Full, PerSentence;
	}

	private static final long serialVersionUID = 8187655380008236328L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "DISPLAY_TYPE", length = 30)
	private DisplayType displayType;

	@ManyToOne
	@JoinColumn(name = "PUBLISHER")
	private Member publisher;

	@OneToMany(mappedBy = "passage", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	private List<Sentence> sentences;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public Passage() {}

	public Passage(Member publisher) {
		super();
		this.publisher = publisher;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Member getPublisher() {	return publisher;}
	public void setPublisher(Member publisher) {this.publisher = publisher;}

	public DisplayType getDisplayType() {return displayType;}
	public void setDisplayType(DisplayType displayType) {this.displayType = displayType;}

	public List<Sentence> getSentences() {return sentences;}
	public void setSentences(List<Sentence> sentences) {this.sentences = sentences;}

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
		Passage other = (Passage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Passage [publisher=");
		builder.append(publisher==null? "null": publisher.getUserId());
		builder.append(", sentences size=");
		builder.append(sentences==null? "null": sentences.size());
		builder.append("]");
		return builder.toString();
	}
}
