package com.esl.entity.rest;

import java.util.List;

public class SaveMemberVocabularyHistoryRequest {
    public long dictationId;
    public int correct;
    public List<VocabPracticeHistory> histories;
}
