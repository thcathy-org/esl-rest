package com.esl.entity.rest;

import java.util.List;

public class CreateDictationHistoryRequest {
    public long dictationId;
    public int mark;
    public int correct;
    public int wrong;
    public List<VocabPracticeHistory> histories;
    public String historyJSON;
}
