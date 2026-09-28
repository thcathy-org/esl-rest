package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;
import com.esl.service.tts.DictationSentenceChunker;
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
	private final Dictation.DictationType type;

	public DictationSearchSummary(long id, String title, Date createdDate, Dictation.StudentLevel suitableStudent,
			int totalAttempt, int totalRecommended, Dictation.Source source, int questionCount, Dictation.DictationType type) {
		this.id = id;
		this.title = title;
		this.createdDate = createdDate;
		this.suitableStudent = suitableStudent;
		this.totalAttempt = totalAttempt;
		this.totalRecommended = totalRecommended;
		this.source = source;
		this.questionCount = questionCount;
		this.type = type;
	}

	/**
	 * One loaded dictation, for a numeric keyword. Same fields as {@link #fromSearchRow}.
	 */
	public static DictationSearchSummary fromDictation(Dictation dictation) {
		var vocabs = dictation.getVocabs();
		return fromSearchRow(dictation.getId(), dictation.getTitle(), dictation.getCreatedDate(),
				dictation.getSuitableStudent(), dictation.getTotalAttempt(), dictation.getTotalRecommended(),
				dictation.getSource(), dictation.getArticle(), vocabs == null ? 0 : vocabs.size());
	}

	/**
	 * Builds one short search hit for a word list or a sentence dictation.
	 * {@code article} chooses {@code type} and the count, then is dropped.
	 * Blank article: {@code type} is Vocab and {@code questionCount} is {@code vocabCount}.
	 * Non-blank article: {@code type} is Article and {@code questionCount} is a 5-word split
	 * (Ionic divideToSentences default), not Dictation.sentenceLength.
	 * {@code type} matches {@link Dictation#getType()}.
	 *
	 * @param vocabCount vocab rows for this dictation
	 */
	public static DictationSearchSummary fromSearchRow(Long id, String title, Date createdDate,
			Dictation.StudentLevel suitableStudent, int totalAttempt, int totalRecommended, Dictation.Source source,
			String article, long vocabCount) {
		var articleDictation = StringUtils.isNotBlank(article);
		var questionCount = articleDictation
				? DictationSentenceChunker.divideToSentences(article, DictationSentenceChunker.WORDS_NORMAL).size()
				: (int) vocabCount;
		var type = articleDictation ? Dictation.DictationType.Article : Dictation.DictationType.Vocab;
		return new DictationSearchSummary(id, title, createdDate, suitableStudent, totalAttempt, totalRecommended,
				source, questionCount, type);
	}

	public long getId() { return id; }
	public String getTitle() { return title; }
	public Date getCreatedDate() { return createdDate; }
	public Dictation.StudentLevel getSuitableStudent() { return suitableStudent; }
	public int getTotalAttempt() { return totalAttempt; }
	public int getTotalRecommended() { return totalRecommended; }
	public Dictation.Source getSource() { return source; }
	public int getQuestionCount() { return questionCount; }
	public Dictation.DictationType getType() { return type; }
}
