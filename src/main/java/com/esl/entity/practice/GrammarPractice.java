package com.esl.entity.practice;

import com.esl.util.practice.GrammarPracticeGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//@Entity
//@Table(name="GRAMMAR_PRACTICE")
public abstract class GrammarPractice implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum PracticeType { Preposition, Article, VerbToBe, SubjectPronoun }

	public enum QuestionFormat { FillInTheBlank }

	//	@Id
	//	@Column(name = "ID")
	//	@GeneratedValue(strategy=GenerationType.AUTO)
	//	private long id;

	//	@Column(name = "CREATED_DATE")
	//	@Temporal(TemporalType.TIMESTAMP)
	//	private java.util.Date createdDate = new java.util.Date();

	//	@Column(name = "LAST_UPDATED_DATE")
	//	@Temporal(TemporalType.TIMESTAMP)
	//	private java.util.Date lastUpdatedDate;

	private String passage;

	private String htmlPassageWithQuestions;

	private List<Integer> questionPositions;

	private String questionReplacePattern;

	private String questionMatchingPattern;

	private String answerReplacePattern;

	//	@ManyToOne(fetch=FetchType.EAGER)
	//	@JoinColumn(name="MEMBER_ID")
	//	private Member member;


	// ********************** Constructors ********************** //
	public GrammarPractice() {}


	// ********************** Accessor Methods ********************** //

	public String getPassage() {return passage;}
	public void setPassage(String passage) {this.passage = passage;}

	public String getHtmlPassageWithQuestions() {return htmlPassageWithQuestions;}
	public void setHtmlPassageWithQuestions(String htmlPassageWithQuestions) {this.htmlPassageWithQuestions = htmlPassageWithQuestions;}

	public List<Integer> getQuestionPositions() {return questionPositions;}
	public void setQuestionPositions(List<Integer> questionPositions) {this.questionPositions = questionPositions;}

	abstract public List<String> getQuestions();
	abstract public void setQuestions(List<String> questions);

	abstract public String getQuestionsRegEx();

	abstract public Object[] getQuestionsString();

	public List<String> getSortedPossibleQuestions() {
		List<String> questions = new ArrayList<String>();
		for (Object o : getQuestionsString()) {
			questions.add(o.toString().toLowerCase());
		}
		Collections.sort(questions);
		return questions;
	}

	public String getQuestionReplacePattern() {return questionReplacePattern;}
	public void setQuestionReplacePattern(String questionReplacePattern) {this.questionReplacePattern = questionReplacePattern;}

	public String getQuestionMatchingPattern() {return questionMatchingPattern;}
	public void setQuestionMatchingPattern(String questionMatchingPattern) {this.questionMatchingPattern = questionMatchingPattern;}

	public String getAnswerReplacePattern() {return answerReplacePattern;}
	public void setAnswerReplacePattern(String answerReplacePattern) {	this.answerReplacePattern = answerReplacePattern;}

	public int getTotalQuestions() { return questionPositions == null ? 0 : questionPositions.size(); }


	// ********************** Common Methods ********************** //
	public void generateQuestions(int maxQ) {
		setQuestions(GrammarPracticeGenerator.retrieveQuestions(this));
		questionPositions = GrammarPracticeGenerator.randomQuestionPositions(getQuestions(), maxQ);
		htmlPassageWithQuestions = GrammarPracticeGenerator.getHTMLPassageWithQuestionNumber(this);
	}


	// ********************** Comparator ********************** //



}
