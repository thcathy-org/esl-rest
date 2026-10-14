package com.esl.entity.dictation;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DictationSearchSort {
	newest,
	mostPracticed,
	mostRecommended,
	lastModified;

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static DictationSearchSort fromJson(String value) {
		if (value == null || value.isBlank()) {
			return newest;
		}
		for (var sort : values()) {
			if (sort.name().equals(value)) {
				return sort;
			}
		}
		return newest;
	}
}
