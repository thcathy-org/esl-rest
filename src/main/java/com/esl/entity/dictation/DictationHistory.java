package com.esl.entity.dictation;

import com.esl.model.Member;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "dictation_history")
public class DictationHistory implements Serializable {
	private static final long serialVersionUID = 2020022531193003055L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name="PRACTICER_NAME")
	private String practicerName;

	@Column(name="PRACTICER_SCHOOL")
	private String practicerSchool;

	@Column(name="MARK")
	private int mark;

	@Column(name = "PRACTICER_AGE_GROUP")
	@Enumerated(EnumType.STRING)
	private Dictation.AgeGroup practicerAgeGroup;

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="DICTATION_ID")
	private Dictation dictation;

	@ManyToOne()
	@JoinColumn(name="MEMBER_ID")
	private Member practicer;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public DictationHistory() {
		createdDate = new Date();
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public String getPracticerName() {
		if (practicer !=null) return practicer.getName().toString();
		return practicerName;
	}
	public void setPracticerName(String practicerName) {this.practicerName = practicerName;}

	public String getPracticerSchool() {
		if (practicer != null) return practicer.getSchool();
		return practicerSchool;
	}
	public void setPracticerSchool(String practicerSchool) {this.practicerSchool = practicerSchool;}

	public int getMark() {return mark;}
	public void setMark(int mark) {this.mark = mark;}

	public Dictation.AgeGroup getPracticerAgeGroup() {return practicerAgeGroup;}
	public void setPracticerAgeGroup(Dictation.AgeGroup practicerAgeGroup) {this.practicerAgeGroup = practicerAgeGroup;}
	public Integer getAgeGroupValue() {
		if (practicerAgeGroup == null) return null;
		else return practicerAgeGroup.ordinal(); }
	public void setAgeGroupValue(Integer ageGroup) {
		if (practicerAgeGroup==null) return;
		this.practicerAgeGroup = Dictation.AgeGroup.values()[ageGroup];
	}

	public Dictation getDictation() {return dictation;}
	public void setDictation(Dictation dictation) {this.dictation = dictation;}

	public Member getPracticer() {return practicer;}
	public void setPracticer(Member practicer) {this.practicer = practicer;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof DictationHistory)) return false;

		final DictationHistory h = (DictationHistory) o;
		return this.id.equals(h.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Dictation History ("); sb.append(getId()); sb.append("): ");
		sb.append("Date["); sb.append(getCreatedDate()); sb.append("] ");
		sb.append("Mark["); sb.append(getMark()); sb.append("] ");
		sb.append("AgeGroup["); sb.append(getPracticerAgeGroup()); sb.append("] ");
		return  sb.toString();
	}
}
