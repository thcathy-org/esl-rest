package com.esl.entity.dictation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "annoymous_dictation_history")
public class AnnoymousDictationHistory implements Serializable {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="DICTATION_ID")
	private Dictation dictation;

	@Column(name = "NAME")
	private String name;

	@Column(name = "SCHOOL")
	private String school;

	@Column(name = "AGE_GROUP")
	@Enumerated(EnumType.STRING)
	private Dictation.AgeGroup ageGroup;

	@Column(name = "MARK")
	private int mark;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public AnnoymousDictationHistory() {
		createdDate = new Date();
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public Dictation getDictation() {return dictation;}
	public void setDictation(Dictation dictation) {this.dictation = dictation;}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	public String getSchool() {	return school;}
	public void setSchool(String school) {	this.school = school;}

	public Dictation.AgeGroup getAgeGroup() {return ageGroup;}
	public void setAgeGroup(Dictation.AgeGroup ageGroup) {this.ageGroup = ageGroup;}

	public int getMark() {	return mark;}
	public void setMark(int mark) {	this.mark = mark;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof AnnoymousDictationHistory)) return false;

		final AnnoymousDictationHistory h = (AnnoymousDictationHistory) o;
		return this.id.equals(h.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Annoymous Dictation History ("); sb.append(getId()); sb.append("): ");
		sb.append("Name["); sb.append(getName()); sb.append("] ");
		sb.append("Mark["); sb.append(getMark()); sb.append("] ");
		return  sb.toString();
	}
}
