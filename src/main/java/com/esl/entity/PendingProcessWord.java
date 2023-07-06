package com.esl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
