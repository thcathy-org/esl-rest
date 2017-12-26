package com.esl.model.practice;

import java.util.Arrays;
import java.util.List;

public class PhoneticSymbol implements Comparable<PhoneticSymbol> {
	public final static String PHONIC_SEPARATOR = ",";
	public final static int LONGEST_PHONIC_LENGTH = 3;
	
	private String id;
	private String[] phonicArray;
		
	// ********************** Constructors ********************** //
	public PhoneticSymbol(String[] phonicArray) {
		this.setPhonicArray(phonicArray);
	}
		
	// ********************** Accessor Methods ********************** //
	public String[] getPhonicArray() {
		return phonicArray;
	}

	public void setPhonicArray(String[] phonicArray) {
		this.phonicArray = phonicArray;				
		this.id = getPhonicId(phonicArray);
	}
	
	public String getId() {
		return id;
	}
	
	public List<String> getPhonicList() {
		return Arrays.asList(phonicArray);
	}
	
	// ********************** Common Methods ********************** //
	public static String getPhonicId(String[] phonicArray) {
		StringBuilder strBuilder = new StringBuilder();
		for (String s : phonicArray) {
			strBuilder.append(s);
			strBuilder.append(PHONIC_SEPARATOR);
		}
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;		
		if (!(o instanceof PhoneticSymbol)) return false;

		final PhoneticSymbol ps = (PhoneticSymbol) o;		
		return this.id.equals(ps.getId());
	}	

	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}
	
	public int compareTo(PhoneticSymbol s) {		
		return this.getId().compareTo(s.getId());		
	}
	
	public String toString() {
		return  "Phonetic Symbol (" + getId() + ")";
	}
}
