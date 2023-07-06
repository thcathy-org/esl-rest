package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "practice_mc_answer")
public class MCAnswer implements Serializable {
	private static final long serialVersionUID = -1331921256925935712L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ANSWER", nullable = false)
	private MCChoice answer;

	@OneToMany(mappedBy = "answer", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	private List<MCChoice> choices;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public MCAnswer() {}

	public MCAnswer(MCChoice answer, List<MCChoice> choices) {
		super();
		this.answer = answer;
		this.choices = choices;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public MCChoice getAnswer() {return answer;}
	public void setAnswer(MCChoice answer) {this.answer = answer;}

	public List<MCChoice> getChoices() {
		if (choices == null) choices = new ArrayList<MCChoice>();
		return choices;
	}
	public void setChoices(List<MCChoice> choices) {this.choices = choices;}
	public void addChoice(MCChoice choice) {
		choice.setAnswer(this);
		choices.add(choice);
	}

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
		MCAnswer other = (MCAnswer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MCAnswer(" + id + ") [answer=" + answer + ", choices=" + choices + "]";
	}
}
