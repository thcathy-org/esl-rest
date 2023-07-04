package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;

import java.util.List;

public class EditDictationRequest {
    public long dictationId = -1;
    public String title;
    public String description;
    public boolean showImage;
    public boolean includeAIImage;
    public List<String> vocabulary;
    public String article;
    public Dictation.StudentLevel suitableStudent;
    public String sentenceLength;
    public boolean wordContainSpace;
    public Dictation.Source source;

    public boolean isCreate() {
        return dictationId < 0;
    }
}
