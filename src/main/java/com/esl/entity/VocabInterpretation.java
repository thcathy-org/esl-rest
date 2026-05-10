package com.esl.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "VOCAB_INTERPRETATION",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_TEXT_LANG",
                columnNames = {"TEXT", "LANG"}))
public class VocabInterpretation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEXT", length = 500, nullable = false)
    private String text;

    @Column(name = "LANG", length = 20, nullable = false)
    private String lang;

    @Column(name = "RESULT", nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(name = "CREATED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();

    public VocabInterpretation() {}

    public VocabInterpretation(String text, String lang, String result) {
        this.text = text;
        this.lang = lang;
        this.result = result;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getLang() { return lang; }
    public String getResult() { return result; }
    public Date getCreatedDate() { return createdDate; }
}
