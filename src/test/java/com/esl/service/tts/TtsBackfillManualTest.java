package com.esl.service.tts;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

class TtsBackfillManualTest {
    private static final Logger logger = LoggerFactory.getLogger(TtsBackfillManualTest.class);
    private static final String STATUS_PENDING = "pending";
    private static final Pattern ENGLISH_OR_DIGIT_PATTERN = Pattern.compile(".*[A-Za-z0-9].*");

    /**
     * Manual JDBC backfill runner (no Spring Boot).
     *
     * Example:
     * ./gradlew test --tests "com.esl.service.tts.TtsBackfillManualTest.backfillArticleDictationsFromId"
     *   -Dbackfill.run=true -Dbackfill.fromId=1000 -Dbackfill.toId=2000 -Dbackfill.dryRun=false
     *
     * Optional DB properties (if not using MYSQL_* env vars):
     * -Dbackfill.jdbcUrl=jdbc:mysql://HOST/DB
     * -Dbackfill.dbUser=...
     * -Dbackfill.dbPassword=...
     *
     * Optional:
     * -Dbackfill.ttsVersion=v2
     * -Dbackfill.toId=999999999 (defaults to Long.MAX_VALUE)
     *
     * Notes:
     * - dryRun=true only counts candidates and does not enqueue.
     * - Running with dryRun=false multiple times will enqueue duplicates.
     */
    @Test
    void backfillArticleDictationsFromId() throws Exception {
        var fromId = 1000;
        var toId = 2000;
        var dryRun = false;
        var ttsVersion = "v2";
        var jdbcUrl = resolveJdbcUrl();
        var dbUser = firstNonBlank(System.getProperty("backfill.dbUser"), System.getenv("MYSQL_USER"));
        var dbPassword = firstNonBlank(System.getProperty("backfill.dbPassword"), System.getenv("MYSQL_PASSWORD"));
        requireNonBlank(dbUser, "Missing DB user. Set -Dbackfill.dbUser or MYSQL_USER.");
        requireNonBlank(dbPassword, "Missing DB password. Set -Dbackfill.dbPassword or MYSQL_PASSWORD.");

        logger.info("Starting TTS backfill: fromId={}, toId={}, dryRun={}, ttsVersion={}, jdbcUrl={}",
                fromId, toId, dryRun, ttsVersion, jdbcUrl);

        var scanned = 0;
        var queuedDictations = 0;
        var queuedRows = 0;
        var skippedBlankArticle = 0;
        var now = new Timestamp(System.currentTimeMillis());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             var selectStmt = connection.prepareStatement(
                     "SELECT ID, ARTICLE FROM dictation WHERE ID >= ? AND ID <= ? ORDER BY ID ASC");
             var insertStmt = connection.prepareStatement(
                     "INSERT INTO TTS_PUBLISH_QUEUE " +
                             "(TTS_VERSION, STATUS, CONTENT, ATTEMPT_COUNT, CREATED_DATE, LAST_UPDATED_DATE) " +
                             "VALUES (?, ?, ?, ?, ?, ?)")) {

            selectStmt.setLong(1, fromId);
            selectStmt.setLong(2, toId);
            try (var rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    scanned++;
                    var article = rs.getString("ARTICLE");
                    if (StringUtils.isBlank(article)) {
                        skippedBlankArticle++;
                        continue;
                    }

                    var uniqueChunks = new LinkedHashSet<String>();
                    for (var maxWords : DictationSentenceChunker.ALL_PRESET_WORD_COUNTS) {
                        uniqueChunks.addAll(DictationSentenceChunker.divideToSentences(article, maxWords));
                    }
                    if (uniqueChunks.isEmpty()) {
                        continue;
                    }

                    queuedDictations++;
                    for (var chunk : uniqueChunks) {
                        var normalizedChunk = chunk.trim();
                        if (!isQueueableContent(normalizedChunk)) {
                            continue;
                        }
                        queuedRows++;
                        if (!dryRun) {
                            insertStmt.setString(1, ttsVersion);
                            insertStmt.setString(2, STATUS_PENDING);
                            insertStmt.setString(3, normalizedChunk);
                            insertStmt.setInt(4, 0);
                            insertStmt.setTimestamp(5, now);
                            insertStmt.setTimestamp(6, now);
                            insertStmt.addBatch();
                        }
                    }
                }
            }

            if (!dryRun) {
                insertStmt.executeBatch();
            }
        }

        logger.info("TTS backfill completed: fromId={}, toId={}, dryRun={}, scanned={}, queuedDictations={}, queuedRows={}, skippedBlankArticle={}",
                fromId, toId, dryRun, scanned, queuedDictations, queuedRows, skippedBlankArticle);
    }

    private static String resolveJdbcUrl() {
        var explicit = System.getProperty("backfill.jdbcUrl");
        if (StringUtils.isNotBlank(explicit)) {
            return explicit;
        }

        var host = "localhost:13306";
        var database = "esl";
        return "jdbc:mysql://" + host + "/" + database;
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.isNotBlank(first)) {
            return first;
        }
        return second;
    }

    private static void requireNonBlank(String value, String errorMessage) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean isQueueableContent(String normalizedChunk) {
        if (StringUtils.isBlank(normalizedChunk)) {
            return false;
        }
        return ENGLISH_OR_DIGIT_PATTERN.matcher(normalizedChunk).matches();
    }
}

