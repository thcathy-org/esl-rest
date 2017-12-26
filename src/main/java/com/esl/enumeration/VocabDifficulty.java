package com.esl.enumeration;

import org.apache.commons.lang3.Range;

public enum VocabDifficulty {
	Beginner(Range.between(1, 500)		, Range.between(1, 4)	, 1),
	Easy(Range.between(501, 1500)		, Range.between(5, 5)	, 2),
	Normal(Range.between(1501, 2500)	, Range.between(6, 6)	, 4),
	Hard(Range.between(2501, 3500)		, Range.between(7, 8)	, 7),
	VeryHard(Range.between(2501, 5000)	, Range.between(9, 30)	, 10);

	public final Range<Integer> rank;
	public final Range<Integer> length;
	public final int weight;

	VocabDifficulty(Range<Integer> rank, Range<Integer> length, int weight) {
		this.rank = rank;
		this.length = length;
		this.weight = weight;
	}

	public boolean disableForVisitor() {
		return !this.equals(Beginner);
	}

}