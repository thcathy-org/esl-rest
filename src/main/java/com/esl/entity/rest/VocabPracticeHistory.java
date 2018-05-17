package com.esl.entity.rest;

import com.esl.model.PhoneticQuestion;

public class VocabPracticeHistory {
    public String answer;
    public boolean correct;
    public PhoneticQuestion question;

    public String getAnswer() {
        return answer;
    }

    public VocabPracticeHistory setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public boolean isCorrect() {
        return correct;
    }

    public VocabPracticeHistory setCorrect(boolean correct) {
        this.correct = correct;
        return this;
    }

    public PhoneticQuestion getQuestion() {
        return question;
    }

    public VocabPracticeHistory setQuestion(PhoneticQuestion question) {
        this.question = question;
        return this;
    }
}
