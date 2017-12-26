package com.esl.model;

import java.io.Serializable;
import java.util.Date;

public class Administrator implements Serializable {
	private Long id = null;
	private String userId;
	private String PIN;
	private Date createdDate = new Date();
	
	// ********************** Constructors ********************** //
	public Administrator() {}
	
	public Administrator(String userId, String PIN) {
		this.userId = userId;
		this.PIN = PIN;
	}
	
	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }
	
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	
	public String getPIN() { return PIN; }
	public void setPIN(String PIN) { this.PIN = PIN; }
		
	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	
	// ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof Administrator)) return false;

		final Administrator admin = (Administrator) o;		
		return this.id.equals(admin.getId());
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "Administrator ('" + getId() + "'), " +
				"User ID: '" + getUserId() + "' ";						
	}
	
}
