package com.esl.entity.practice.qa;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "irregular_verb")
public class IrregularVerb implements Serializable {
	private static final long serialVersionUID = -725981238442874786L;

	@Id
	@Column(name = "PRESENT", length = 30)
	private String present;

	@Column(name = "PRESENT_PARTICIPLE", length = 30)
	private String presentParticiple;

	@Column(name = "PAST", length = 30)
	private String past;

	@Column(name = "PAST_PARTICIPLE", length = 30)
	private String pastParticiple;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public IrregularVerb() {}

	public IrregularVerb(String present, String presentParticiple, String past, String pastParticiple) {
		super();
		this.present = present;
		this.presentParticiple = presentParticiple;
		this.past = past;
		this.pastParticiple = pastParticiple;
	}

	// ********************** Accessor Methods ********************** //
	public String getPresent() {return present;}
	public void setPresent(String present) {this.present = present;}

	public String getPresentParticiple() {return presentParticiple;}
	public void setPresentParticiple(String presentParticiple) {this.presentParticiple = presentParticiple;}

	public String getPast() {return past;}
	public void setPast(String past) {this.past = past;}

	public String getPastParticiple() {return pastParticiple;}
	public void setPastParticiple(String pastParticiple) {this.pastParticiple = pastParticiple;}

	public Date getCreatedDate() {return createdDate;}
	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}

	// ********************** Common Methods ********************** //

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((present == null) ? 0 : present.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		IrregularVerb other = (IrregularVerb) obj;
		if (present == null) {
			if (other.present != null)
				return false;
		} else if (!present.equals(other.present))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("IrregularVerb (%s) [past=%s, pastParticiple=%s, presentParticiple=%s]", present, past, pastParticiple, presentParticiple);
	}
}
