package com.esl.entity.rest;

import java.util.List;

public class SaveMemberVocabularyHistoryRequest {
    public long dictationId;
    public List<VocabPracticeHistory> histories;

    public int totalCorrect() {
        return (int) histories.stream().filter(VocabPracticeHistory::isCorrect).count();
    }
}
