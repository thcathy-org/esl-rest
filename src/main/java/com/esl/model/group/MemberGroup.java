package com.esl.model.group;

import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberGroup implements Serializable {
	private static Logger logger = LoggerFactory.getLogger(MemberGroup.class);
	
	private Long id;
	private String title;
	private String PIN;
	private Member admin;
	private List<Member> members = new ArrayList<Member>();
	private List<MemberGroupMessage> messages = new ArrayList<MemberGroupMessage>();
	private Date createdDate = new Date();	
	
	//	 ********************** Constructors ********************** //
	public MemberGroup() {}
	
	public MemberGroup(String title, Member admin) {
		this.title = title;
		this.setAdmin(admin);
	}

	//	 ********************** Accessor Methods ********************** //
	public Member getAdmin() {	return admin;}
	public void setAdmin(Member admin) {	this.admin = admin;	}

	public Date getCreatedDate() {	return createdDate;	}
	public void setCreatedDate(Date createdDate) {	this.createdDate = createdDate;	}

	public Long getId() {	return id;	}
	public void setId(Long id) {	this.id = id;	}

	public List<Member> getMembers() {	return members;	}
	public void setMembers(List<Member> members) {	this.members = members;	}
	public void addMember(Member member) {
		if (member == null) throw new IllegalArgumentException("Can't add a null member.");
		logger.info("addMember: Add member");
		this.members.add(member);
	}
	
	public String getPIN() {	return PIN;	}
	public void setPIN(String pin) {	PIN = pin;	}

	public String getTitle() {return title;	}
	public void setTitle(String title) {this.title = title;	}
	
	public List<MemberGroupMessage> getMessages() {	return messages;}
	public void setMessages(List<MemberGroupMessage> messages) {this.messages = messages;	}
	public void addMessage(MemberGroupMessage message) {
		if (message == null) throw new IllegalArgumentException("Can't add a null message.");
		logger.info("addMessage: Add message subject");
		message.setGroup(this);
		this.messages.add(message);
	}

	//	 ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof MemberGroup)) return false;

		final MemberGroup g = (MemberGroup) o;		
		return this.id == g.getId();
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "MemberGroup (" + getId() + "): " +
				"Title[" + getTitle() + "] " +
				"Admin[" + getAdmin().getUserId() + "] ";				
	}	
	
}
	