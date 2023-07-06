package com.esl.model;

import jakarta.persistence.*;
import java.util.Comparator;
import java.util.Date;

@Entity
@Table(name = "practice_result")
public class PracticeResult {
	// practice types
	final static public String PHONETICPRACTICE = "PHONETICPRACTICE";
	final static public String PHONETICSYMBOLPRACTICE = "PHONETICSYMBOLPRACTICE";
	final static public String COMPREHENSIONPRACTICE = "COMPREHENSIONPRACTICE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PRACTICE_RESULT_ID")
	private Long id = null;

	@Column(name = "MARK")
	private int mark = 0;

	@Column(name = "FULL_MARK")
	private int fullMark = 0;

	@Column(name = "RATE")
	private double rate = 0.0;

	@Column(name = "TOTAL_PRACTICES")
	private int totalPractices = 0;

	@Column(name = "PRACTICE_TYPE")
	private String practiceType;

	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name = "MEMBER_ID")
	private Member member;

	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name = "GRADE_ID")
	private Grade grade;

	@Column(name = "LEVEL")
	private String level;

	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	@Transient
	private String levelTitle;

	// ********************** Constructors ********************** //
	public PracticeResult() {}

	public PracticeResult(Member member, Grade grade, String practiceType ) {
		this.member = member;
		this.grade = grade;
		this.practiceType = practiceType;
	}

	public PracticeResult(Member member, Grade grade, String practiceType, String level) {
		this(member, grade, practiceType);
		this.level = level;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public int getMark() { return mark; }
	public void setMark(int mark) {
		this.mark = mark;
		if (fullMark > 0) this.rate = (double)this.mark / (double)this.fullMark;
	}

	public int getFullMark() {	return fullMark;}
	public void setFullMark(int fullMark) {
		this.fullMark = fullMark;
		if (fullMark > 0) this.rate = (double)this.mark / (double)this.fullMark;
	}

	public double getRate() {return rate;}
	public void setRate(double rate) {this.rate = rate;}

	public int getTotalPractices() {return totalPractices;}
	public void setTotalPractices(int totalPractices) {	this.totalPractices = totalPractices;}

	public String getPracticeType() {return practiceType;}
	public void setPracticeType(String practiceType) {this.practiceType = practiceType;	}

	public Member getMember() { return member; }
	public void setMember(Member member) { this.member = member; }

	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {this.grade = grade;}

	public String getLevel() {return level;}
	public void setLevel(String level) {this.level = level;}

	public String getLevelTitle() {	return levelTitle;}
	public void setLevelTitle(String levelTitle) {	this.levelTitle = levelTitle;}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof PracticeResult)) return false;

		final PracticeResult ps = (PracticeResult) o;
		return this.id.equals(ps.getId());
	}

	@Override
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		return  "Practice Result (" + getId() + ") [ " +
		"Member'" + getMember() + "' " +
		"Grade'" + getGrade() + "' " +
		"Level'" + getLevel() + "' " +
		"Mark'" + getMark() + "' " +
		"Full Mark'" + getFullMark() + "' " +
		"Total Practices'" + getTotalPractices() + "' " +
		"Practice Type'" + getPracticeType() + "' " +
		" ]";
	}

	// ********************** Public Functions ********************** //
	public void addResult(int mark, int fullMark) {
		this.mark += mark;
		this.fullMark += fullMark;
		if (!(this.fullMark == 0))
			this.rate = (double)this.mark / (double)this.fullMark;
		this.totalPractices++;
	}

	// ********************** Comparator ********************** //
	public static class TopScoreComparator implements Comparator<PracticeResult> {
		@Override
		public int compare(PracticeResult o1, PracticeResult o2) {
			if (o2.getMark() == o1.getMark()) {
				if (o2.getRate() == o1.getRate())
					return o1.getCreatedDate().compareTo(o2.getCreatedDate());
				else
					return (o2.getRate() - o1.getRate()) > 0?1:-1;
			}
			return o2.getMark() - o1.getMark();
		}
	}

	public static class TopRateComparator implements Comparator<PracticeResult> {
		@Override
		public int compare(PracticeResult o1, PracticeResult o2) {
			if (o2.getRate() == o1.getRate()){
				if (o2.getMark() == o1.getMark())
					return o1.getCreatedDate().compareTo(o2.getCreatedDate());
				else
					return o2.getMark() - o1.getMark();
			}
			return (o2.getRate() - o1.getRate()) > 0?1:-1;
		}
	}
}
