package com.esl.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "testing", havingValue = "false", matchIfMissing = true)
public class PhoneticQuestionWordUniqueIndex {
    static final String INDEX_NAME = "UK_PHONETIC_QUESTION_WORD";
    private static final Logger log = LoggerFactory.getLogger(PhoneticQuestionWordUniqueIndex.class);

    private final JdbcTemplate jdbc;

    public PhoneticQuestionWordUniqueIndex(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void ensure() {
        if (uniqueIndexExists()) {
            return;
        }
        var duplicates = jdbc.query(
                "SELECT WORD FROM phonetic_question WHERE WORD IS NOT NULL GROUP BY WORD HAVING COUNT(*) > 1",
                (rs, rowNum) -> rs.getString(1));
        if (!duplicates.isEmpty()) {
            var sample = duplicates.stream().limit(20).collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "Refusing to add unique index " + INDEX_NAME
                            + " on phonetic_question.WORD; duplicate WORDs exist: " + sample);
        }
        try {
            jdbc.execute("ALTER TABLE phonetic_question ADD CONSTRAINT " + INDEX_NAME + " UNIQUE (WORD)");
            log.info("Added unique index {} on phonetic_question.WORD", INDEX_NAME);
        } catch (DataAccessException e) {
            if (uniqueIndexExists()) {
                return;
            }
            throw e;
        }
    }

    private boolean uniqueIndexExists() {
        return Boolean.TRUE.equals(jdbc.execute((ConnectionCallback<Boolean>) con -> indexExists(con)));
    }

    private static boolean indexExists(Connection con) throws SQLException {
        var product = con.getMetaData().getDatabaseProductName();
        if (product != null && product.toLowerCase().contains("h2")) {
            return count(con, """
                    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                    WHERE LOWER(TABLE_NAME) = 'phonetic_question'
                      AND LOWER(CONSTRAINT_NAME) = ?
                      AND CONSTRAINT_TYPE = 'UNIQUE'
                    """) > 0;
        }
        return count(con, """
                SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND LOWER(TABLE_NAME) = 'phonetic_question'
                  AND LOWER(CONSTRAINT_NAME) = ?
                  AND CONSTRAINT_TYPE = 'UNIQUE'
                """) > 0;
    }

    private static int count(Connection con, String sql) throws SQLException {
        try (var ps = con.prepareStatement(sql)) {
            ps.setString(1, INDEX_NAME.toLowerCase());
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
