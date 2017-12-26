package com.esl.model;

import com.esl.model.practice.PhoneticSymbols.Level;

import java.util.TreeSet;

public class TopResult {

	public enum OrderType { Score, Rate };

	TreeSet<PracticeResult> topResults;
	String title;
	String practiceTitle;
	String longTitle;
	String gradeTitle;
	String orderTypeTitle;
	String practiceType;
	String levelTitle;
	int firstPosition;
	Grade grade;
	Level level;
	OrderType orderType;

	// ============== Setter / Getter ================//
	public TreeSet<PracticeResult> getTopResults() {return topResults;	}
	public void setTopResults(TreeSet<PracticeResult> topResults) {	this.topResults = topResults;}

	public PracticeResult[] getTopResultsArray() {
		if (topResults != null) {
			return topResults.toArray(new PracticeResult[topResults.size()]);
		} else {
			return new PracticeResult[]{};
		}
	}

	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}

	public String getPracticeTitle() {	return practiceTitle;}
	public void setPracticeTitle(String practiceTitle) {this.practiceTitle = practiceTitle;	}

	public String getGradeTitle() {	return gradeTitle;	}
	public void setGradeTitle(String gradeTitle) {this.gradeTitle = gradeTitle;}

	public String getLongTitle() {return longTitle;}
	public void setLongTitle(String longTitle) {this.longTitle = longTitle;}

	public String getPracticeType() {return practiceType;}
	public void setPracticeType(String practiceType) {this.practiceType = practiceType;	}

	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {	this.grade = grade;	}

	public OrderType getOrderType() {return orderType;	}
	public void setOrderType(OrderType orderType) {	this.orderType = orderType;	}

	public String getOrderTypeTitle() {	return orderTypeTitle;	}
	public void setOrderTypeTitle(String orderTypeTitle) {	this.orderTypeTitle = orderTypeTitle; }

	public String getLevelTitle() {	return levelTitle;}
	public void setLevelTitle(String levelTitle) {	this.levelTitle = levelTitle;}

	public int getFirstPosition() {	return firstPosition;}
	public void setFirstPosition(int firstPosition) {this.firstPosition = firstPosition;}

	public Level getLevel() {return level;	}
	public void setLevel(Level level) {	this.level = level;	}

	// ============== Constructors ================//
	public TopResult() {}

	public TopResult(OrderType orderType) {
		this.orderType = orderType;
		switch (orderType) {
		case Score:
			topResults = new TreeSet<PracticeResult>(new PracticeResult.TopScoreComparator()); break;
		case Rate:
			topResults = new TreeSet<PracticeResult>(new PracticeResult.TopRateComparator()); break;
		}
	}

	// ********************** Common Methods ********************** //
	@Override
	public String toString() {
		return  "Top Result ('" + getTitle() + "') [ " +
				"practiceType'" + getPracticeType() + "' " +
				"orderType'" + getOrderType() + "' " +
				"grade'" + getGrade() + "' " +
				"level'" + getLevel() + "' " +
				"first pos'" + getFirstPosition() + "' " +
				"Total pract result'" + getTopResults() + "' " +
				" ]";
	}

	static public void main(String... x) {
		System.out.println(OrderType.Rate);
	}
}
