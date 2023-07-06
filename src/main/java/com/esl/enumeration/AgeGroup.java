package com.esl.enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum AgeGroup {
	@Column(name="AGE_GROUP")
	@Enumerated(EnumType.STRING)
	Age3to6("3-6",3,6), Age7to9("7-9",7,9), Age10to12("10-12",10,12), Age13to15("13-15",13,15), Age16to18("16-18",16,18), AgeOver18(">18",19,100), AgeAny("Any",-1,-1);

	private final String display;
	public final int minAge;
	public final int maxAge;
	private AgeGroup(String display, int minAge, int maxAge) {
		this.display = display;
		this.minAge = minAge;
		this.maxAge = maxAge;
	}
	public int getMinAge() { return minAge; }
	public int getMaxAge() { return maxAge; }

	@Override
	public String toString() {	return display;	}
	public String getString() { return display; }

	public static AgeGroup getAgeGroup(int age) {
		for (AgeGroup a : AgeGroup.values()) {
			if (age >= a.minAge && age <=a.maxAge) return a;
		}
		return AgeAny;
	}
}
