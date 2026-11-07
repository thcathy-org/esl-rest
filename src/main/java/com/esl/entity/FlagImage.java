package com.esl.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "flag_image", uniqueConstraints = @UniqueConstraint(
        name = "UK_FLAG_IMAGE_PHRASE_INDEX",
        columnNames = {"PHRASE", "IMAGE_INDEX"}))
public class FlagImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PHRASE", nullable = false, length = 500)
    private String phrase;

    @Column(name = "IMAGE_INDEX", nullable = false)
    private int imageIndex;

    @Column(name = "FLAG_COUNT", nullable = false)
    private int flagCount;

    @Column(name = "CREATED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhrase() { return phrase; }
    public void setPhrase(String phrase) { this.phrase = phrase; }

    public int getImageIndex() { return imageIndex; }
    public void setImageIndex(int imageIndex) { this.imageIndex = imageIndex; }

    public int getFlagCount() { return flagCount; }
    public void setFlagCount(int flagCount) { this.flagCount = flagCount; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
}
