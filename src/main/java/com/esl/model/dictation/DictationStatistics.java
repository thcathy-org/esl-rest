package com.esl.model.dictation;

import com.esl.entity.dictation.Dictation;

import java.util.ArrayList;
import java.util.List;

public class DictationStatistics {
	public enum Type {
		MostPracticed, NewCreated, MostRecommended, LatestPracticed;
	}

	private Type type;
	private List<Dictation> dictations = new ArrayList<Dictation>();

	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public List<Dictation> getDictations() {
		return dictations;
	}
	public void setDictations(List<Dictation> dictations) {
		this.dictations = dictations;
	}
}
