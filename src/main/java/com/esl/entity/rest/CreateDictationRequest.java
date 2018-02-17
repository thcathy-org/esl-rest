package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;

import java.util.List;

public class CreateDictationRequest {
    public String title;
    public String description;
    public boolean showImage;
    public List<String> vocabulary;
    public Dictation.StudentLevel suitableStudent;
}
