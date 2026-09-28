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

	/**
	 * When true, {@code POST /dictation/search} returns {@link DictationSearchSummary} rows
	 * (id, title, createdDate, suitableStudent, totalAttempt, totalRecommended, source,
	 * questionCount, type) and omits vocabs, article, and creator.
	 * {@code type} is Vocab or Article, the same value as {@link Dictation#getType()}.
	 * Default false keeps the full {@code Dictation} payload for installed apps.
	 */
	public boolean shortPayload = false;

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

	public SearchDictationRequest setShortPayload(boolean shortPayload) {
		this.shortPayload = shortPayload;
		return this;
	}
}
