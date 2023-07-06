package com.esl.dao.dictation;

import com.esl.dao.ESLDao;
import com.esl.entity.dictation.Vocab;
import com.esl.entity.dictation.VocabHistory;
import com.esl.exception.IllegalParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Query;
import java.util.Collection;

@Transactional
@Repository("vocabHistoryDAO")
public class VocabHistoryDAO extends ESLDao<VocabHistory> implements IVocabHistoryDAO {
	private static Logger logger = LoggerFactory.getLogger(VocabHistoryDAO.class);


	public int removeByVocabs(Collection<Vocab> vocabs) {
		final String logPrefix = "removeByVocabs: ";
		logger.info(logPrefix + "START");
		if (vocabs == null) throw new IllegalParameterException(new String[]{"vocabs"}, new Object[]{vocabs});

		logger.info(logPrefix + "input vocab size[" + vocabs.size() + "]");
		if (vocabs.size() < 1) return 0;
		String queryStr = "DELETE FROM VocabHistory AS h WHERE h.vocab in (:vocabs)";
		Query query = em.createQuery(queryStr).setParameter("vocabs", vocabs);
		int result = query.executeUpdate();
		logger.info(logPrefix + "deleted history size[" + result + "]");
		return result;
	}
}
