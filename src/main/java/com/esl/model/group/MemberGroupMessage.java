package com.esl.model.group;

import com.esl.model.Member;

import java.io.Serializable;
import java.util.Date;

public class MemberGroupMessage implements Serializable {
	private Long id;
	private MemberGroup group;
	private Member member;
	private String message = "";
	private String subject = "";
	private Date createdDate = new Date();
	
	
	//	 ********************** Constructors ********************** //
	public MemberGroupMessage() {}
	
	public MemberGroupMessage(MemberGroup group, Member member) {
		this.group = group;
		this.member = member;
	}

	//	 ********************** Accessor Methods ********************** //
	public String getMessage() {return message;}
	public void setMessage(String message) {this.message = message;	}

	public String getSubject() {return subject;	}
	public void setSubject(String subject) {this.subject = subject;	}	
	
	public Date getCreatedDate() {return createdDate;	}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;	}

	public Long getId() {return id;	}
	public void setId(Long id) {this.id = id;	}

	public MemberGroup getGroup() {	return group;	}
	public void setGroup(MemberGroup group) {this.group = group;	}

	public Member getMember() {return member;	}
	public void setMember(Member member) {this.member = member;	}

	//	 ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof MemberGroupMessage)) return false;

		final MemberGroupMessage g = (MemberGroupMessage) o;		
		return this.id == g.getId();
	}
	
	public int hashCode() {	return id==null ? System.identityHashCode(this) : id.hashCode();}
	
	public String toString() {
		return  "MemberGroupActivityLog (" + getId() + "): " +
				"Group Title[" + getGroup().getTitle() + "] " +
				"Member ID[" + getMember().getUserId() + "] " +
				"Subject[" + getSubject() + "] " +
				"createdDate[" + getCreatedDate() + "]";				
	}

	
	
}
	