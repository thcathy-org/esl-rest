package com.esl.entity.event;

import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;
import com.esl.model.PhoneticQuestion;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdatePracticeHistoryEvent {
    public final Member member;
    public final ESLPracticeType type;
    public final PhoneticQuestion question;
    public final boolean isCorrect;
    public final int score;

    public UpdatePracticeHistoryEvent(Member member, ESLPracticeType type, PhoneticQuestion question, boolean isCorrect, int score) {
        this.member = member;
        this.type = type;
        this.question = question;
        this.isCorrect = isCorrect;
        this.score = score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", member!=null?member.getUserId():"null")
                .append("type", type)
                .append("question", question)
                .append("isCorrect", isCorrect)
                .append("score", score)
                .toString();
    }
}
