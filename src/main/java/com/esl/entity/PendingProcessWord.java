package com.esl.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pending_process_word")
public class PendingProcessWord {
    @Id
    private String word;

    public String getWord() {
        return word;
    }

    public PendingProcessWord setWord(String word) {
        this.word = word;
        return this;
    }
}
