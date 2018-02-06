package com.esl.entity.rest;

import java.util.List;

public class CreateDictationHistoryRequest {
    public long dictationId;
    public int mark;
    public List<VocabPracticeHistory> histories;
}
