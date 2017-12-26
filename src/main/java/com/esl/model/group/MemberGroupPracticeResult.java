package com.esl.model.group;

import com.esl.model.*;

import java.io.Serializable;
import java.util.Date;

public class MemberGroupPracticeResult implements Serializable {	
	private Long id;
	private MemberGroup group;
	private String practiceType;
	private int mark;
	private int fullMark;
	private Grade grade;
	private Date createdDate;
		
	//	 ********************** Constructors ********************** //
	public MemberGroupPracticeResult() {}
		

	//	 ********************** Accessor Methods ********************** //
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
	
	public int getFullMark() {return fullMark;}
	public void setFullMark(int fullMark) {this.fullMark = fullMark;}

	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {this.grade = grade;}

	public MemberGroup getGroup() {return group;}
	public void setGroup(MemberGroup group) {this.group = group;}

	public int getMark() {return mark;}
	public void setMark(int mark) {this.mark = mark;}

	public String getPracticeType() {return practiceType;}
	public void setPracticeType(String practiceType) {this.practiceType = practiceType;	}
	
	public Date getCreatedDate() {return createdDate;	}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;	}
	
	public double getRate() {
		if (fullMark  == 0) return 0.0;
		return mark/fullMark;
	}
	
	//	 ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof MemberGroupPracticeResult)) return false;
		
		final MemberGroupPracticeResult g = (MemberGroupPracticeResult) o;		
		return (this.id == g.getId());
	}
		
	public String toString() {
		return  "MemberGroupPracticeResult: " +
				"Group Title[" + getGroup().getTitle() + "] " +
				"Type[" + getPracticeType() + "] " +
				"Mark[" + getMark() + "] " +
				"FullMark[" + getFullMark() + "] " +
				"createdDate[" + getCreatedDate() + "]";				
	}


	
}
	