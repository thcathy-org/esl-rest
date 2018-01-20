package com.esl.dao;

import com.esl.entity.VocabImage;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("vocabImageDAO")
public class VocabImageDAO extends ESLDao<VocabImage> implements IVocabImageDAO {
	private static Logger log = LoggerFactory.getLogger(VocabImageDAO.class);

	@Override
	@Transactional(readOnly = true)
	public List<VocabImage> listByWord(String word) {
		log.info("listByWord: {}", word);
		String queryStr = "SELECT i FROM VocabImage i WHERE i.word = :word";
		javax.persistence.Query query = em.createQuery(queryStr).setParameter("word", word);
		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<VocabImage> listLatest(int maxRow, int maxId) {
		log.info("listLatest {} row from id {}", maxRow, maxId);
		String queryStr = "SELECT i FROM VocabImage i WHERE i.id < :maxid ORDER BY i.createdDate DESC";
		javax.persistence.Query query = em.createQuery(queryStr).setParameter("maxid", maxId);
		query.setMaxResults(maxRow);
		return query.getResultList();
	}

}
