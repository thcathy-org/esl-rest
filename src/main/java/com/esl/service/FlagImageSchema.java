package com.esl.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FlagImageSchema {
    static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS flag_image (
                ID BIGINT NOT NULL AUTO_INCREMENT,
                PHRASE VARCHAR(500) NOT NULL,
                IMAGE_INDEX INT NOT NULL,
                FLAG_COUNT INT NOT NULL,
                CREATED_DATE DATETIME(6) NOT NULL,
                PRIMARY KEY (ID),
                CONSTRAINT UK_FLAG_IMAGE_PHRASE_INDEX UNIQUE (PHRASE, IMAGE_INDEX)
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public FlagImageSchema(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureTable() {
        jdbcTemplate.execute(CREATE_TABLE);
    }
}
