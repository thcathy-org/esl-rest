package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_mc_question")
public class MCQuestion implements Serializable {
	private static final long serialVersionUID = 6875190134499694551L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "TITLE", length = 1000)
	private String title;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ANSWER")
	private MCAnswer answer;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public MCQuestion() {}

	public MCQuestion(String title, MCAnswer answer) {
		super();
		this.title = title;
		this.answer = answer;
	}



	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}

	public MCAnswer getAnswer() {return answer;}
	public void setAnswer(MCAnswer answer) {this.answer = answer;}

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
		MCQuestion other = (MCQuestion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MCQuestion (" + id + ") [title=" + title + "]";
	}
}
