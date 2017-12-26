package com.esl.model;

import com.esl.enumeration.VocabDifficulty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class PhoneticPractice implements Serializable {
	private static final long serialVersionUID = 4137007197852825623L;

	public static int MAX_QUESTIONS = 10;

	private Member member;
	private Grade grade;
	private VocabDifficulty difficulty;
	private List<PhoneticQuestion> questions = new ArrayList<>();
	private ArrayList<String> answers = new ArrayList<>();
	private ArrayList<Boolean> corrects = new ArrayList<>();
	private int currentQuestion = 0;
	private int mark = 0;
	private int totalQuestions = MAX_QUESTIONS;
	private Date startTime;
	private Date endTime;

	// ********************** Constructors ********************** //
	public PhoneticPractice() {
		startTime = new Date();
	}

	// ********************** Accessor Methods ********************** //
	public int getMaxQuestions() {	return MAX_QUESTIONS;}
	@Value("${PhoneticPractice.MaxQuestions}") public void setMaxQuestions(int max_questions) {MAX_QUESTIONS = max_questions;	}

	public Member getMember() {	return member;}
	public void setMember(Member member) {this.member = member;}

	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {	this.grade = grade;	}

	public List<PhoneticQuestion> getQuestions() {return questions;}
	public void setQuestions(List<PhoneticQuestion> questions) {this.questions = questions;}
	public void addQuestions(PhoneticQuestion question) {questions.add(question);}

	public List<String> getAnswers() {return answers;}
	public void setAnswers(ArrayList<String> answers) {this.answers = answers;}
	public void addAnswers(String answer) {answers.add(answer);}
	public void addAnswers(int index, String answer) {answers.add(index, answer);}

	public List<Boolean> getCorrects() {return corrects;}
	public void setCorrects(ArrayList<Boolean> corrects) {this.corrects = corrects;}
	public void addCorrects(boolean correct) {corrects.add(correct);}
	public void addCorrects(int index, boolean correct) {corrects.add(index, correct);}

	public int getCurrentQuestion() {return currentQuestion;}
	public void setCurrentQuestion(int currentQuestion) {this.currentQuestion = currentQuestion;}
	public PhoneticQuestion getCurrentQuestionObject() {return questions.get(currentQuestion);}

	public int getMark() {return mark;}
	public void setMark(int mark) {this.mark = mark;}
	public void addMark(int offset) {this.mark += offset;}

	public Date getStartTime() {return startTime;}
	public void setStartTime(Date startTime) {this.startTime = startTime;}

	public Date getEndTime() {return endTime;}
	public void setEndTime(Date endTime) {this.endTime = endTime;}

	public int getTotalQuestions() { return totalQuestions; }
	public void setTotalQuestions(int totalQuestions) { this.totalQuestions =totalQuestions; }

	public VocabDifficulty getDifficulty() {return difficulty;}
	public void setDifficulty(VocabDifficulty difficulty) {this.difficulty = difficulty;}

	// ********************** Supporting Methods ********************** //
	public boolean isFinish() {
		return (currentQuestion >= totalQuestions);
	}

	public boolean isLastQuestion() {
		return (currentQuestion == totalQuestions-1);
	}
}
