package com.esl.entity.practice.qa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_mc_choice")
public class MCChoice implements Serializable {
	private static final long serialVersionUID = 9212084472423195801L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "CONTENT", length = 1000)
	private String content;

	@ManyToOne()
	@JoinColumn(name = "MC_ANSWER")
	private MCAnswer answer;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public MCChoice() {}

	public MCChoice(String content) {
		this.content = content;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getContent() { return content;}
	public void setContent(String content) {this.content = content;}

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
		MCChoice other = (MCChoice) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MCChoice(" + id + ") [content=" + content + ", id=" + id + "]";
	}
}
