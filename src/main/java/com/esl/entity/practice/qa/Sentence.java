package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_sentence")
public class Sentence implements Serializable {
	private static final long serialVersionUID = -7435388658195851675L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "CONTENT", length = 1000)
	private String content;

	@Column(name = "ORDER_IN_PASSAGE")
	private int orderInPassage;

	@ManyToOne
	@JoinColumn(name = "PASSAGE")
	private Passage passage;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public Sentence() {}

	public Sentence(String content) {
		this.content = content;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getContent() { return content;}
	public void setContent(String content) {this.content = content;}

	public int getOrderInPassage() {return orderInPassage;}
	public void setOrderInPassage(int orderInPassage) {this.orderInPassage = orderInPassage;}

	public Passage getPassage() {return passage;}
	public void setPassage(Passage passage) {this.passage = passage;}

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
		Sentence other = (Sentence) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sentence(" + id + ") [order=" + orderInPassage + ",content=" + content + "]";
	}
}
