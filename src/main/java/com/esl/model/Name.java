package com.esl.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Embeddable
public class Name implements Serializable {
	@Size(max=100, message="{incorrectSize}")
	@Pattern(regexp="[a-zA-Z\\s]*", message="{alphaOnly}")
	@Column(name = "LAST_NAME")
	private String lastName;
	
	@Size(max=100, message="{incorrectSize}")
	@Pattern(regexp="[a-zA-Z\\s]*", message="{alphaOnly}")
	@Column(name = "FIRST_NAME")
	private String firstName;
	
	// ********************** Constructors ********************** //
	public Name() {}
	public Name(String lastName, String firstName) {
		this.lastName = lastName;
		this.firstName = firstName;
	}
	
	// ********************** Accessor Methods ********************** //
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	
	// ********************** Common Methods ********************** //
	public String toString() {	return getFullName(); }
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Name)) return false;
		final Name name = (Name) o;
		if (getFullName() != null ? !getFullName().equals(name.getFullName()) : name.getFullName().equals(" ")) return false;			
		return true;
	}
	
	public String getFullName() {
		if (lastName != null)
			return firstName + " " + lastName;
		else
			return firstName;
	}
}
