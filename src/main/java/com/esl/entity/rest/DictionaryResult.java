package com.esl.entity.rest;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class DictionaryResult {
    public String word;
    public String pronunciationUrl;
    public String pronunciationLang;
    public String IPA;
    public String definition;

    public DictionaryResult() {}

    public DictionaryResult(String word, String pronunciationUrl, String pronunciationLang, String IPA, String definition) {
        this.word = word;
        this.pronunciationUrl = pronunciationUrl;
        this.pronunciationLang = pronunciationLang;
        this.IPA = IPA;
        this.definition = definition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("word", word)
                .append("pronunciationUrl", pronunciationUrl)
                .append("pronunciationLang", pronunciationLang)
                .append("IPA", IPA)
                .append("definition", definition)
                .toString();
    }
}
