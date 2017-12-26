package com.esl.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Receipt implements Serializable {
	private Long id = null;
	private String tutorialCentre;
	private String tutorialClass;
	private String receiptNumber;
	private Member owner = null;
	private Date createdDate = new Date();
	private Date issuedDate = new Date();
	private Date expiredDate = new Date();
	
	// ********************** Constructors ********************** //
	public Receipt() {}
	
	public Receipt(String tutorialCentre, String tutorialClass, String receiptNumber, Member owner, Date issuedDate, Date expiredDate) {
		this.tutorialCentre = tutorialCentre;
		this.tutorialClass = tutorialClass;
		this.receiptNumber = receiptNumber;
		this.owner = owner;
		this.issuedDate = issuedDate;
		this.expiredDate = expiredDate;
	}
	
	public Receipt(String tutorialCentre, String tutorialClass, String receiptNumber, Date issuedDate) {
		this.tutorialCentre = tutorialCentre;
		this.tutorialClass = tutorialClass;
		this.receiptNumber = receiptNumber;
		setIssuedDate(issuedDate);
	}
	
	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }
	
	public String getTutorialCentre() { return tutorialCentre; }
	public void setTutorialCentre(String tutorialCentre) { this.tutorialCentre = tutorialCentre; }
	
	public String getTutorialClass() { return tutorialClass; }
	public void setTutorialClass(String tutorialClass) { this.tutorialClass = tutorialClass; }
	
	public String getReceiptNumber() { return receiptNumber; }
	public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
	
	public Member getOwner() { return owner; }
	public void setOwner(Member owner) { this.owner = owner; }
	
	public Date getCreatedDate() { return createdDate; }
	private void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	
	public Date getIssuedDate() { return issuedDate; }
	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
		if (expiredDate.equals(createdDate))							// default add three month from issued date to expired Date
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(issuedDate);
			calendar.add(Calendar.MONTH, 3);
			expiredDate = calendar.getTime();			
		}
	}
	
	public Date getExpiredDate() { return expiredDate; }
	public void setExpiredDate(Date exCalendar) { this.expiredDate = expiredDate; }
	
	// ********************** Common Methods ********************** //
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof Receipt)) return false;

		final Receipt receipt = (Receipt) o;		
		return this.id.equals(receipt.getId());
	}
	
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public String toString() {
		return  "Receipt ('" + getId() + "'), " +
				"Tutorial Centre: '" + getTutorialCentre() + "' " +
				"Tutorial Class: '" + getTutorialClass() + "' " +
				"Receipt Number: '" + getReceiptNumber() + "' " +
				"Created Date: '" + getCreatedDate().toString() + "' " +
				"Issued Date: '" + getIssuedDate().toString() + "' " +
				"Expired Date: '" + getExpiredDate().toString() + "' ";
	}

	public int compareTo(Object o) {
		if (o instanceof Receipt) {
			return this.getCreatedDate().compareTo( ((Receipt)o).getCreatedDate() );
		}
		return 0;
	}
}
