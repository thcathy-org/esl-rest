package com.esl.entity;

import java.util.Date;

public interface IAuditable {

	public Date getCreatedDate();
	public void setCreatedDate(Date createdDate);

	public Date getLastUpdatedDate();
	public void setLastUpdatedDate(Date lastUpdatedDate);

}
