package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;

public class EditDictationRequest {
    public String title;
    public String description;
    public String[] suitableAges;
    public String[] tags;
    public Dictation.DictationType type;
    public boolean showImage;
    public String vocabs;
}
