package com.esl.model.group;

import com.esl.model.Member;

import java.io.Serializable;
import java.util.Date;

public class MemberGroupActivityLog implements Serializable {
	// Constant action Type
	public final static String CREATE_GROUP	= "CREATE_GROUP";
	public final static String JOIN_GROUP		= "JOIN_GROUP";
	public final static String LEAVE_GROUP		= "LEAVE_GROUP";
	public final static String REMOVE_GROUP	= "REMOVE_GROUP";
	public final static String CHANGE_PIN		= "CHANGE_PIN";
	public final static String CHANGE_TITLE	= "CHANGE_TITLE";
	public final static String KICK_MEMBER		= "KICK_MEMBER";
	public final static String DELETE_MESSAGE	= "DELETE_MESSAGE";
	public final static String EDIT_MESSAGE	= "EDIT_MESSAGE";
	public final static String ADD_MESSAGE		= "ADD_MESSAGE";
	
	private Long id;
	private MemberGroup group;
	private Member member;
	private String actionType;
	private String comment;
	private Date createdDate = new Date();
	
	
	//	 ********************** Constructors ********************** //
	public MemberGroupActivityLog() {}
	
	public MemberGroupActivityLog(MemberGroup group, Member member, String actionType) {
		this.group = group;
		this.member = member;
		this.actionType = actionType;
	}

	//	 ********************** Accessor Methods ********************** //
	public String getActionType() {return actionType;	}
	public void setActionType(String actionType) {this.actionType = actionType;	}

	public String getComment() {return comment;	}
	public void setComment(String comment) {this.comment = comment;	}

	public Date getCreatedDate() {return createdDate;	}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;	}

	public Long getId() {return id;	}
	public void setId(Long id) {this.id = id;	}

	public MemberGroup getGroup() {return group;}
	public void setGroup(MemberGroup group) {this.group = group;}

	public Member getMember() {return member;}
	public void setMember(Member member) {this.member = member;}

	//	 ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof MemberGroupActivityLog)) return false;

		final MemberGroupActivityLog g = (MemberGroupActivityLog) o;		
		return this.id == g.getId();
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "MemberGroupActivityLog (" + getId() + "): " +
				"ActionType[" + getActionType() + "] " +
				"createdDate[" + getCreatedDate() + "]";				
	}	
	
}
	