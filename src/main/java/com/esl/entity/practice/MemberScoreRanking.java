package com.esl.entity.practice;

import java.util.List;

public class MemberScoreRanking {
	List<MemberScore> scores;
	MemberScore base;
	boolean isTop;
	long firstPosition;

	// ============== Setter / Getter ================//
	public List<MemberScore> getScores() {return scores;}

	public MemberScore getBase() {return base;}

	public boolean isTop() {return isTop;}

	public long getFirstPosition() { return firstPosition;	}

	// ============== Constructors ================//
	public MemberScoreRanking() {}

	public MemberScoreRanking(MemberScore base, List<MemberScore> scores, boolean isTop, long firstPosition) {
		this.scores = scores;
		this.base = base;
		this.isTop = isTop;
		this.firstPosition = firstPosition;
	}

	// ********************** Common Methods ********************** //

}
