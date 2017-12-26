package com.esl.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Embeddable
public abstract class AuditableObject {


	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();

	@Column(name = "LAST_UPDATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedDate;



	public Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}

	public Date getLastUpdatedDate() {return lastUpdatedDate;}
	public void setLastUpdatedDate(Date lastUpdatedDate) {this.lastUpdatedDate = lastUpdatedDate;}



}
