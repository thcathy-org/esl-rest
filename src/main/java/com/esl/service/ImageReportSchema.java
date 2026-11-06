package com.esl.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ImageReportSchema {
    static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS image_report (
                ID BIGINT NOT NULL AUTO_INCREMENT,
                PHRASE VARCHAR(500) NOT NULL,
                IMAGE_INDEX INT NOT NULL,
                MEMBER_ID BIGINT NULL,
                CREATED_DATE DATETIME(6) NOT NULL,
                DONE BIT(1) NOT NULL,
                PRIMARY KEY (ID)
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public ImageReportSchema(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureTable() {
        jdbcTemplate.execute(CREATE_TABLE);
    }
}
