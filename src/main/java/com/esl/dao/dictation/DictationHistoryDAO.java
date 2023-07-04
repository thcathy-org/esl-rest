package com.esl.dao.dictation;

import com.esl.dao.ESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationHistory;
import com.esl.exception.IllegalParameterException;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;

@Repository("dictationHistoryDAO")
public class DictationHistoryDAO extends ESLDao<DictationHistory> implements IDictationHistoryDAO {
	private static Logger logger = LoggerFactory.getLogger(DictationHistoryDAO.class);

	public List<DictationHistory> listByDictation(Dictation dictation) {
		return listByDictation(dictation, 0);
	}

	@Transactional(readOnly=true)
	public List<DictationHistory> listByDictation(Dictation dictation, int maxResult) {
		logger.info("listByDictation: START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		logger.info("listByDictation: input dictation[" + dictation + "], maxResult[" + maxResult + "]");
		String queryStr = "FROM DictationHistory h WHERE h.dictation = :dictation ORDER BY h.createdDate DESC";
		Query query = em.createQuery(queryStr).setParameter("dictation", dictation);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional(readOnly=true)
	public List<DictationHistory> listAnnoymousHistoryByDictation(Dictation dictation, int maxResult) {
		logger.info("listAnnoymousHistoryByDictation: START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		logger.info("listAnnoymousHistoryByDictation: input dictation[" + dictation + "], maxResult[" + maxResult + "]");
		String queryStr = "FROM DictationHistory h WHERE h.dictation = :dictation AND h.practicer IS NULL ORDER BY h.createdDate DESC";
		Query query = em.createQuery(queryStr).setParameter("dictation", dictation);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional(readOnly=true)
	public List<DictationHistory> listByMember(Member member, int maxResult) {
		logger.info("listByMember: START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		logger.info("listByMember: input member[" + member.getUserId() + "], maxResult[" + maxResult + "]");
		String queryStr = "SELECT h FROM DictationHistory h JOIN h.dictation WHERE h.practicer = :member ORDER BY h.createdDate DESC";
		Query query = em.createQuery(queryStr).setParameter("member", member);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional
	public int removeByDictation(Dictation dictation) {
		final String logPrefix = "removeByDictation: ";
		logger.info(logPrefix + "START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		String queryStr = "DELETE FROM DictationHistory AS h WHERE h.dictation = :dictation";
		Query query = em.createQuery(queryStr).setParameter("dictation", dictation);
		int result = query.executeUpdate();
		logger.info(logPrefix + "deleted history size[" + result + "]");
		return result;
	}

	@Transactional(readOnly=true)
	public Optional<DictationHistory> getLastestOfAllDictationByMember(Member member) {
		logger.info("getLastestOfAllDictationByMember: START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "FROM DictationHistory h WHERE h.dictation.creator = :member ORDER BY h.createdDate DESC";
		Query query = em.createQuery(queryStr);
		query.setParameter("member", member);
		query.setMaxResults(1);
		var result = query.getResultList();
		return result.size() > 0 ? Optional.of((DictationHistory) result.get(0)) : Optional.empty();
	}
}
