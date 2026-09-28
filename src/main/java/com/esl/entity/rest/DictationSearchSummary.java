package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;
import com.esl.service.tts.DictationSentenceChunker;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Short row for {@code POST /dictation/search} when {@link SearchDictationRequest#shortPayload} is true.
 * Vocabs, article text, and creator are not included.
 */
public class DictationSearchSummary {
	private final long id;
	private final String title;
	private final Date createdDate;
	private final Dictation.StudentLevel suitableStudent;
	private final int totalAttempt;
	private final int totalRecommended;
	private final Dictation.Source source;
	private final int questionCount;
	private final boolean sentenceDictation;

	public DictationSearchSummary(long id, String title, Date createdDate, Dictation.StudentLevel suitableStudent,
			int totalAttempt, int totalRecommended, Dictation.Source source, int questionCount, boolean sentenceDictation) {
		this.id = id;
		this.title = title;
		this.createdDate = createdDate;
		this.suitableStudent = suitableStudent;
		this.totalAttempt = totalAttempt;
		this.totalRecommended = totalRecommended;
		this.source = source;
		this.questionCount = questionCount;
		this.sentenceDictation = sentenceDictation;
	}

	/**
	 * @param article used only to compute {@code questionCount} and {@code sentenceDictation}; not stored
	 * @param vocabCount vocab rows for this dictation
	 */
	public static DictationSearchSummary fromSearchRow(Long id, String title, Date createdDate,
			Dictation.StudentLevel suitableStudent, int totalAttempt, int totalRecommended, Dictation.Source source,
			String article, long vocabCount) {
		var sentenceDictation = StringUtils.isNotBlank(article);
		// List label uses a 5-word split (Ionic divideToSentences default), not Dictation.sentenceLength.
		var questionCount = sentenceDictation
				? DictationSentenceChunker.divideToSentences(article, DictationSentenceChunker.WORDS_NORMAL).size()
				: (int) vocabCount;
		return new DictationSearchSummary(id, title, createdDate, suitableStudent, totalAttempt, totalRecommended,
				source, questionCount, sentenceDictation);
	}

	public long getId() { return id; }
	public String getTitle() { return title; }
	public Date getCreatedDate() { return createdDate; }
	public Dictation.StudentLevel getSuitableStudent() { return suitableStudent; }
	public int getTotalAttempt() { return totalAttempt; }
	public int getTotalRecommended() { return totalRecommended; }
	public Dictation.Source getSource() { return source; }
	public int getQuestionCount() { return questionCount; }

	@JsonProperty("sentenceDictation")
	public boolean isSentenceDictation() { return sentenceDictation; }
}
