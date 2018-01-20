package com.esl.dao;

import com.esl.model.Grade;
import com.esl.model.PhoneticQuestion;
import com.esl.util.practice.PhoneticQuestionUtil;
import com.esl.util.practice.PhoneticQuestionUtil.FindIPAAndPronoun;
import org.apache.commons.lang3.Range;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Repository("phoneticQuestionDAO")
public class PhoneticQuestionDAO extends ESLDao<PhoneticQuestion> {
	private static final String GET_PHONETIC_QUESTION_BY_WORD = "from PhoneticQuestion phoneticQuestion where phoneticQuestion.word = :word";
	private static final String GET_QUESTIONS_BY_GRADE = "SELECT pq.phoneticquestion_id id FROM phonetic_question pq, grade_phoneticquestion gpq WHERE pq.phoneticquestion_id = gpq.phoneticquestion_id AND gpq.grade_id = :gradeId LIMIT 0, :total";
	private static final String GET_NOT_ENRICHED_QUESTIONS = "from PhoneticQuestion pq where pq.IPA is null or pq.pronouncedLink is null";
	private final Logger logger = LoggerFactory.getLogger(PhoneticQuestionDAO.class);
	
	public PhoneticQuestionDAO() {}

	public PhoneticQuestion getPhoneticQuestionByWord(String word) {
		List result = em.createQuery(GET_PHONETIC_QUESTION_BY_WORD).setParameter("word", word).getResultList();
		if (result.size() > 0)
			return (PhoneticQuestion) result.get(0);
		else {
			logger.info("Cannot find the PhoneticQuestion by word:" + word);
			return null;
		}
	}

	public List<PhoneticQuestion> getNotEnrichedQuestions() {
		return em.createQuery(GET_NOT_ENRICHED_QUESTIONS).setMaxResults(300).getResultList();
	}

	public List<PhoneticQuestion> getRandomQuestionWithinRank(Range<Integer> rank, int totalResult) {
		String queryString = "FROM PhoneticQuestion pq WHERE pq.rank >= :fromRank and pq.rank <= :toRank ORDER BY RAND()";
		javax.persistence.Query query = em.createQuery(queryString);
		query.setParameter("fromRank", rank.getMinimum());
		query.setParameter("toRank", rank.getMaximum());
		query.setMaxResults(totalResult);

		return query.getResultList();
	}

	public List<PhoneticQuestion> getRandomQuestionWithinLength(Range<Integer> length, int totalResult) {
		String queryString = "FROM PhoneticQuestion pq WHERE length(pq.word) >= :fromLength and length(pq.word) <= :toLength ORDER BY RAND()";
		javax.persistence.Query query = em.createQuery(queryString);
		query.setParameter("fromLength", length.getMinimum());
		query.setParameter("toLength", length.getMaximum());
		query.setMaxResults(totalResult);

		return query.getResultList();
	}

	public List<PhoneticQuestion> getRandomQuestionsByGrade(Grade grade, int total, boolean isRandom) {
		List<PhoneticQuestion> questions = new ArrayList<PhoneticQuestion>();
		String queryString = "SELECT pq.phoneticquestion_id id FROM phonetic_question pq, grade_phoneticquestion gpq WHERE pq.phoneticquestion_id = gpq.phoneticquestion_id AND gpq.grade_id = :gradeId";
		if (isRandom) queryString += " ORDER BY RAND()";

		// Get Questions ID
		javax.persistence.Query query = em.createNativeQuery(queryString);
		query.setParameter("gradeId", grade.getId());
		query.setMaxResults(total);
		logger.info("getRandomQuestionsByGrade: queryString[" + queryString + "]");

		List<Long> results = query.getResultList();
		if (results.size() < 1) {
			logger.info("Cannot get any PhoneticQuestion by SQL:" + queryString);
			return null;
		}

		List<Thread> threads = new ArrayList<Thread>();
		PhoneticQuestionUtil pqUtil = new PhoneticQuestionUtil();

		// Generate the questions List
		if (results.size() < total) total = results.size();
		for (int i=0; i < total; i++) {
			PhoneticQuestion question = em.find(PhoneticQuestion.class, results.get(i));
			if (question != null) {
				FindIPAAndPronoun finder = pqUtil.new FindIPAAndPronoun(questions, question, null, null);
				Thread newThread = new Thread(finder);
				logger.info("Start a new thread for Find IPA and Pronoun with id:" + newThread.getId() + ", Word[" + question.getWord()  + "]");
				newThread.start();
				threads.add(newThread);
			} else {
				logger.info("Cannot find the PhoneticQuestion by id:" + results.get(i));
			}
		}
		logger.info("All thread for Find IPA and Pronoun STARTED");

		while (threads.size() > 0 && questions.size() < total) {
			logger.info("questions.size()=" + questions.size());
			try {
				synchronized (this) {
					for (int i = 0; i < threads.size(); i++) {
						if (!threads.get(i).isAlive())
							threads.remove(i);
					}
					Thread.sleep(250);	// 0.25 sec
				}
			} catch (InterruptedException ex) {
				logger.error("Interrupted when sleep", ex);
			}
		}

		return questions;
	}

}
