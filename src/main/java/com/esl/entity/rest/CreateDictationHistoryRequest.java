package com.esl.entity.rest;

import java.util.List;

public class CreateDictationHistoryRequest {
    public long dictationId;
    public int mark;
    public int fullMark;
    public double percentage;
    public List<VocabPracticeHistory> histories;
    public String historyJSON;
}
