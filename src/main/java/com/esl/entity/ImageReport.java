package com.esl.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "image_report")
public class ImageReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PHRASE", nullable = false, length = 500)
    private String phrase;

    @Column(name = "IMAGE_INDEX", nullable = false)
    private int imageIndex;

    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "CREATED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "DONE", nullable = false)
    private boolean done;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhrase() { return phrase; }
    public void setPhrase(String phrase) { this.phrase = phrase; }

    public int getImageIndex() { return imageIndex; }
    public void setImageIndex(int imageIndex) { this.imageIndex = imageIndex; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
