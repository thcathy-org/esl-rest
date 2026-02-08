package com.esl.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "TTS_PUBLISH_QUEUE")
public class TtsPublishQueue {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_FAILED = "failed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TTS_VERSION")
    private String ttsVersion;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "ATTEMPT_COUNT")
    private int attemptCount;

    @Column(name = "NEXT_ATTEMPT_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttemptAt;

    @Column(name = "LAST_ERROR")
    private String lastError;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "LAST_UPDATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTtsVersion() { return ttsVersion; }
    public void setTtsVersion(String ttsVersion) { this.ttsVersion = ttsVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public Date getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Date nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(Date lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
}
