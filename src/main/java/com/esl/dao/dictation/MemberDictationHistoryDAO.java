package com.esl.dao.dictation;

import com.esl.dao.ESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.MemberDictationHistory;
import com.esl.exception.IllegalParameterException;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Query;
import java.util.List;
@Repository("memberDictationHistoryDAO")
public class MemberDictationHistoryDAO extends ESLDao<MemberDictationHistory> implements IMemberDictationHistoryDAO {
	private static Logger logger = LoggerFactory.getLogger(MemberDictationHistoryDAO.class);

	@Resource private IVocabHistoryDAO vocabHistoryDAO;

	public void setVocabHistoryDAO(IVocabHistoryDAO vocabHistoryDAO) {this.vocabHistoryDAO = vocabHistoryDAO; }

	@Transactional(readOnly=true)
	public List<MemberDictationHistory> listByDictation(Dictation dictation) {
		return listByDictation(dictation, 0);
	}

	@Transactional(readOnly=true)
	public List<MemberDictationHistory> listByDictation(Dictation dictation, int maxResult) {
		logger.info("listByDictation: START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		logger.info("listByDictation: input dictation[" + dictation + "], maxResult[" + maxResult + "]");
		String queryStr = "FROM MemberDictationHistory h WHERE h.dictation = :dictation ORDER BY h.lastPracticeDate DESC";
		Query query = em.createQuery(queryStr).setParameter("dictation", dictation);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional(readOnly=true)
	public List<MemberDictationHistory> listByMember(Member member, int maxResult) {
		logger.info("listByMember: START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		logger.info("listByMember: input member[" + member.getUserId() + "], maxResult[" + maxResult + "]");
		String queryStr = "FROM MemberDictationHistory h WHERE h.owner = :member ORDER BY h.lastPracticeDate DESC";
		Query query = em.createQuery(queryStr).setParameter("member", member);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional(readOnly=true)
	public MemberDictationHistory loadByDictationMember(Member member, Dictation dictation) {
		final String logPrefix = "loadByDictationMember: ";
		logger.info(logPrefix + "START");
		if (member == null || dictation == null) throw new IllegalParameterException(new String[]{"member","dictation"}, new Object[]{member, dictation});
		logger.info(logPrefix + "input member[" + member.getUserId() + "], dictation[" + dictation.getId() + "]");
		String queryStr = "FROM MemberDictationHistory h WHERE h.owner = :member AND h.dictation = :dictation";
		Query query = em.createQuery(queryStr).setParameter("member", member).setParameter("dictation", dictation);
		return (MemberDictationHistory) query.getResultList().get(0);
	}

	@Transactional
	public int removeByDictation(Dictation dictation) {
		final String logPrefix = "removeByDictation: ";
		logger.info(logPrefix + "START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		// remove all vocab histories with dictation
		String deleteVocabHistoriesQuery = "DELETE FROM VocabHistory AS h WHERE h.dictationHistory in (FROM MemberDictationHistory AS mdh WHERE mdh.dictation = :dictation)";
		Query query = em.createQuery(deleteVocabHistoriesQuery).setParameter("dictation", dictation);
		int result = query.executeUpdate();
		logger.info(logPrefix + "deleted vocab history size[" + result + "]");

		// remove member dictation history
		String deleteDictationHistoriesQuery = "DELETE FROM MemberDictationHistory AS mdh WHERE mdh.dictation = :dictation";
		query = em.createQuery(deleteDictationHistoriesQuery).setParameter("dictation", dictation);
		result = query.executeUpdate();
		logger.info(logPrefix + "deleted member dictation history size[" + result + "]");

		return result;
	}

	@Transactional(readOnly=true)
	public MemberDictationHistory getLastestByMember(Member member) {
		final String logPrefix = "getLastestByMember: ";
		logger.info(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "FROM MemberDictationHistory h WHERE h.owner = :member ORDER BY h.createdDate DESC";
		Query query = em.createQuery(queryStr);
		query.setParameter("member", member);
		query.setMaxResults(1);

		return (MemberDictationHistory) query.getResultList().get(0);
	}

}
