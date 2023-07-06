package com.esl.entity.dictation;

import com.esl.entity.practice.qa.Sentence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.List;


public class PassageDictation extends Dictation {
	@OneToMany(mappedBy="dictation", cascade={CascadeType.ALL}, fetch= FetchType.EAGER)
	private List<Sentence> sentence;
	
	// ********************** Constructors ********************** //
	public PassageDictation() {
		
	}
	
	// ********************** Accessor Methods ********************** //
	public List<Sentence> getSentence() {return sentence;}
	public void setSentence(List<Sentence> sentence) {this.sentence = sentence;}

	// ********************** Common Methods ********************** //

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PassageDictation ("); sb.append(getId()); sb.append(")");
		return  sb.toString();
	}

	
}
