package com.esl.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "grade")
public class Grade implements Serializable, Comparable  {
	@Id
	@Column(name = "GRADE_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "LONG_TITLE")
	private String longTitle;

	@Transient
	private String description;

	@Column(name = "LEVEL")
	private int level;

	@Column(name = "PHONETIC_PRACTICE_LV_UP_REQUIRE")
	private int phoneticPracticeLvUpRequire;

	@Column(name = "PHONETIC_SYMBOL_PRACTICE_LV_UP_REQUIRE")
	private int phoneticSymbolPracticeLvUpRequire;

	@Transient
	private List<PhoneticQuestion> phoneticQuestions = new ArrayList<PhoneticQuestion>();

	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();


	// ********************** Constructors ********************** //
	public Grade() {}

	public Grade(String title, int level) {
		this.title = title;
		this.level = level;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Date getCreatedDate() { return createdDate; }
	private void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getLongTitle() {return longTitle;}
	public void setLongTitle(String longTitle) {this.longTitle = longTitle;}

	public int getLevel() { return level; }
	public void setLevel(int level) { this.level = level; }

	public String getDescription() {return description;}
	public void setDescription(String description) {this.description = description;	}

	public int getPhoneticPracticeLvUpRequire() {return phoneticPracticeLvUpRequire;}
	public void setPhoneticPracticeLvUpRequire(int phoneticPracticeLvUpRequire) {this.phoneticPracticeLvUpRequire = phoneticPracticeLvUpRequire;}

	public int getPhoneticSymbolPracticeLvUpRequire() {	return phoneticSymbolPracticeLvUpRequire;}
	public void setPhoneticSymbolPracticeLvUpRequire(int phoneticSymbolPracticeLvUpRequire) {this.phoneticSymbolPracticeLvUpRequire = phoneticSymbolPracticeLvUpRequire;}

	public List<PhoneticQuestion> getPhoneticQuestions() { return phoneticQuestions; }
	public void setPhoneticQuestions(List<PhoneticQuestion> phoneticQuestions) { this.phoneticQuestions = phoneticQuestions; }
	public void addPhoneticQuestions(PhoneticQuestion question)
	{
		if (question == null) throw new IllegalArgumentException("Can't add a null question.");

		question.getGrades().add(this);
		this.phoneticQuestions.add(question);
	}
	
	public boolean isNotTopGrade() {
		return level != 14;
	}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Grade)) return false;

		final Grade grade = (Grade) o;
		return this.id.doubleValue() == grade.getId().doubleValue();
	}

	@Override
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		return  "Grade ('" + getId() + "'), " +
		"Title: '" + getTitle() + "' " +
		"Description: '" + getDescription() + "' " +
		"Level: '" + getLevel() + "' ";
	}

	public int compareTo(Object o) {
		if (o instanceof Grade) {
			return this.getLevel() - ((Grade)o).getLevel();
		}
		return 0;
	}

}
