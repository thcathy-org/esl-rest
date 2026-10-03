package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;

import java.io.Serializable;
import java.util.Date;

public class SearchDictationRequest implements Serializable {
	public String keyword;
	public boolean searchTitle = true;
	public boolean searchDescription = true;
	public Date minDate;
	public Date maxDate;
	public String creator;
	public Dictation.StudentLevel suitableStudent;
	public String type;

	public SearchDictationRequest setKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public SearchDictationRequest setSearchTitle(boolean searchTitle) {
		this.searchTitle = searchTitle;
		return this;
	}

	public SearchDictationRequest setSearchDescription(boolean searchDescription) {
		this.searchDescription = searchDescription;
		return this;
	}

	public SearchDictationRequest setMinDate(Date minDate) {
		this.minDate = minDate;
		return this;
	}

	public SearchDictationRequest setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
		return this;
	}

	public SearchDictationRequest setCreator(String creator) {
		this.creator = creator;
		return this;
	}

    public SearchDictationRequest setSuitableStudent(Dictation.StudentLevel suitableStudent) {
        this.suitableStudent = suitableStudent;
        return this;
    }

    public SearchDictationRequest setType(String type) {
		this.type = type;
		return this;
	}
}
