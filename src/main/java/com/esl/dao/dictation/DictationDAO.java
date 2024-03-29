package com.esl.dao.dictation;

import com.esl.dao.ESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationSearchCriteria;
import com.esl.entity.dictation.Vocab;
import com.esl.exception.IllegalParameterException;
import com.esl.model.Member;
import com.esl.model.group.MemberGroup;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.esl.entity.dictation.DictationSearchCriteria.*;

@Transactional
@Repository("dictationDAO")
public class DictationDAO extends ESLDao<Dictation> implements IDictationDAO {
	private static Logger logger = LoggerFactory.getLogger(DictationDAO.class);

	@Autowired
	private IVocabDAO vocabDAO;
	@Autowired private IMemberDictationHistoryDAO memberDictationHistoryDAO;
	@Autowired private IDictationHistoryDAO dictationHistoryDAO;

	public void setVocabDAO(IVocabDAO vocabDAO) {this.vocabDAO = vocabDAO;}
	public void setMemberDictationHistoryDAO(IMemberDictationHistoryDAO memberDictationHistoryDAO) {this.memberDictationHistoryDAO = memberDictationHistoryDAO;}
	public void setDictationHistoryDAO(IDictationHistoryDAO dictationHistoryDAO) {this.dictationHistoryDAO = dictationHistoryDAO;}

	public List<Dictation> listByMember(Member member) {
		logger.info("listByMember: START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		logger.info("listByDictation: input member[" + member.getUserId() + "]");
		String queryStr = "FROM Dictation d WHERE d.creator = :member ORDER BY d.id DESC";
		jakarta.persistence.Query query = em.createQuery(queryStr).setParameter("member", member);
		return query.getResultList();
	}

	public List<Dictation> listHighestRating(int minRated, int maxResult) {
		logger.info("listHighestRatingDictation: START");
		String queryStr = "FROM Dictation d WHERE d.totalRated >= :minRated AND d.isPublicAccess = TRUE ORDER BY d.rating DESC, d.totalRated DESC";
		Query query = em.createQuery(queryStr).setParameter("minRated", minRated);
		return query.setMaxResults(maxResult).getResultList();
	}

	public List<Dictation> listMostPracticed(int maxResult) {
		logger.info("listMostPracticeDictation: START");
		String queryStr = "FROM Dictation d WHERE d.isPublicAccess = TRUE and d.source != 'Select' ORDER BY d.totalAttempt DESC, d.rating DESC";
		return em.createQuery(queryStr).setMaxResults(maxResult).getResultList();
	}

	public List<Dictation> listLatestPracticed(int maxResult) {
		logger.info("listLatestPracticed: START");
		String queryStr = "FROM Dictation d WHERE d.isPublicAccess = TRUE AND d.source != 'Select' ORDER BY d.lastPracticeDate DESC";
		return em.createQuery(queryStr).setMaxResults(maxResult).getResultList();
	}

	public List<Dictation> listNewCreated(int maxResult) {
		logger.info("listNewCreated: START");
		String queryStr = "FROM Dictation d WHERE d.isPublicAccess = TRUE and d.source != 'Select' ORDER BY d.createdDate DESC";
		return em.createQuery(queryStr).setMaxResults(maxResult).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Dictation> listMostRecommended(int minRecommended, int maxResult) {
		logger.info("listMostRecommended: START");
		String queryStr = "FROM Dictation d WHERE d.totalRecommended >= :minRecommended AND d.isPublicAccess = TRUE AND d.source != 'Select' ORDER BY d.totalRecommended DESC, d.createdDate DESC";
		Query query = em.createQuery(queryStr).setParameter("minRecommended", minRecommended);
		return query.setMaxResults(maxResult).getResultList();
	}

	public List<Dictation> listByMemberGroup(MemberGroup group, int maxResult) {
		logger.info("listByMemberGroup: START");
		if (group == null) throw new IllegalParameterException(new String[]{"group"}, new Object[]{group});

		logger.info("listByDictation: input group[" + group.getTitle() + "], maxResult[" + maxResult + "]");
		String queryStr = "SELECT d FROM Dictation d JOIN d.accessibleGroups as group WHERE group = :group ORDER BY d.lastPracticeDate DESC";
		Query query = em.createQuery(queryStr).setParameter("group", group);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}
			
	@Transactional
	public void remove(Dictation dictation) {
		logger.info("remove: START");
		if (dictation == null) throw new IllegalParameterException(new String[]{"dictation"}, new Object[]{dictation});

		memberDictationHistoryDAO.removeByDictation(dictation);
		vocabDAO.removeByDictation(dictation);
		dictationHistoryDAO.removeByDictation(dictation);
		delete(dictation);
	}

	public void refreshDictation(Dictation dictation) {
		if (dictation.getId() != null) {
			refresh(dictation);
			for (Vocab vocab : dictation.getVocabs()) {
				refresh(vocab);
			}
		}
	}

	@Transactional(readOnly = true)
	public List<Dictation> searchDictation(Map<DictationSearchCriteria, Object> searchCriteria, int maxResult) {
		final String logPrefix = "searchDictation: ";
		logger.info(logPrefix + "START");

		// construct query string
		StringBuilder querySB = new StringBuilder();
		querySB.append("SELECT DISTINCT d FROM Dictation d WHERE d.source=com.esl.entity.dictation.Dictation$Source.FillIn ");
		querySB.append(getSearchDictationWhereClause(searchCriteria));
		querySB.append(" ORDER BY d.lastModifyDate DESC, d.rating DESC, d.totalRated DESC");
		logger.info(logPrefix + "queryStr[" + querySB.toString() + "]");

		Query query = em.createQuery(querySB.toString());
		putSearchDictationParams(query, searchCriteria);
		if (maxResult > 0) query.setMaxResults(maxResult);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Dictation randomAccessibleDictation(Member member) {
		final String logPrefix = "randomAccessibleDictation: ";
		logger.info(logPrefix + "START");

		// construct query string
		StringBuilder querySB = new StringBuilder();
		querySB.append("SELECT DISTINCT d FROM Dictation d WHERE (d.password IS NULL OR d.password = '')");
		if (member == null) {
			querySB.append(" AND d.isPublicAccess = TRUE");
		} else {
			querySB.append(" AND (d.isPublicAccess = TRUE OR d.creator = :creator");
			//if (member.getGroups() != null && member.getGroups().size() > 0) querySB.append(" OR g in (:groups)");
			querySB.append(")");
		}
		querySB.append(" ORDER BY RAND()");
		logger.info(logPrefix + "queryStr[" + querySB.toString() + "]");

		Query query = em.createQuery(querySB.toString());
		if (member != null) {
			query.setParameter("creator", member);
		//	if (member.getGroups() != null && member.getGroups().size() > 0) query.setParameterList("groups", member.getGroups());
		}
		query.setMaxResults(1);
		return (Dictation)query.getResultList().get(0);
	}

	@Transactional(readOnly = true)
	public int getTotalDictationByMember(Member member) {
		final String logPrefix = "getTotalDictationByMember: ";
		logger.info(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "SELECT COUNT(*) as counter FROM Dictation d WHERE d.creator = :creator";
		Query query = em.createQuery(queryStr);
		query.setParameter("creator", member);

		List results = query.getResultList();
		if (results.size() < 1) {
			logger.warn(logPrefix + "no counter retrieved");
			return -1;
		}

		Long counter = (Long) results.get(0);
		if (counter == null) return 0;
		return counter.intValue();
	}

	@Transactional(readOnly = true)
	public int getTotalAttemptedByMember(Member member) {
		final String logPrefix = "getTotalAttemptedByMember: ";
		logger.info(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "SELECT SUM(d.totalAttempt) as counter FROM Dictation d WHERE d.creator = :creator";
		Query query = em.createQuery(queryStr);
		query.setParameter("creator", member);

		List results = query.getResultList();
		if (results.size() < 1) {
			logger.warn(logPrefix + "no total retrieved");
			return -1;
		}

		Long counter = (Long) results.get(0);
		if (counter == null) return 0;
		return counter.intValue();
	}

	@Transactional(readOnly = true)
	public Dictation getMostAttemptedByMember(Member member) {
		final String logPrefix = "getMostAttemptedByMember: ";
		logger.info(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "FROM Dictation d WHERE d.creator = :creator ORDER BY d.totalAttempt DESC";
		Query query = em.createQuery(queryStr);
		query.setParameter("creator", member);
		query.setMaxResults(1);

		return (Dictation) query.getResultList().get(0);
	}

	@Transactional(readOnly = true)
	public Dictation getHighestRatingByMember(Member member) {
		final String logPrefix = "getHighestRatingByMember: ";
		logger.info(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "FROM Dictation d WHERE d.creator = :creator ORDER BY d.rating DESC";
		Query query = em.createQuery(queryStr);
		query.setParameter("creator", member);
		query.setMaxResults(1);

		return (Dictation) query.getResultList().get(0);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Dictation getMostRecommendedByMember(Member member) {
		final String logPrefix = "getMostRecommendedByMember: ";
		logger.debug(logPrefix + "START");
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		final String queryStr = "FROM Dictation d WHERE d.creator = :creator ORDER BY d.totalRecommended DESC";
		Query query = em.createQuery(queryStr);
		query.setParameter("creator", member);
		query.setMaxResults(1);
		return (Dictation) query.getResultList().get(0);

	}
	
	// ---------------- supporting function -------------- //
	private String getSearchDictationWhereClause(Map<DictationSearchCriteria, Object> searchCriteria) {
		StringBuilder clause = new StringBuilder();
		// Age
		if (searchCriteria.containsKey(MinAge) && searchCriteria.containsKey(MaxAge)) {
			clause.append(" AND (d.suitableMinAge = -1 OR (d.suitableMinAge <= :" + DictationSearchCriteria.MaxAge + " AND d.suitableMaxAge >= :" + DictationSearchCriteria.MinAge + "))");
		}
		// Creator name
		if (searchCriteria.containsKey(CreatorName)) {
			for (String s : ((String)searchCriteria.get(CreatorName)).split(" ")) {
				clause.append(" AND LOWER(CONCAT(d.creator.name.firstName,d.creator.name.lastName,d.creator.emailAddress)) like '%" + s.toLowerCase() + "%'");
			}
		}

		// Title, description, tags
		if (searchCriteria.containsKey(Title) || searchCriteria.containsKey(Description)) {
			StringBuilder concatSB = new StringBuilder("LOWER(CONCAT(''");
			if (searchCriteria.containsKey(Title)) concatSB.append(",d.title,' '");
			if (searchCriteria.containsKey(Description)) concatSB.append(",d.description,' '");
			concatSB.append("))");

			if (searchCriteria.containsKey(Title)) {
				String[] titles = ((String)searchCriteria.get(Title)).split(" ");
				for (int i=0; i < titles.length; i++) clause.append(" AND " + concatSB.toString() + " like :" + Title + i);
			}
			if (searchCriteria.containsKey(Description)) {
				String[] descs = ((String)searchCriteria.get(Description)).split(" ");
				for (int i=0; i < descs.length; i++) clause.append(" AND " + concatSB.toString() + " like :" + Description + i);
			}
		}

		// Date range
		if (searchCriteria.containsKey(MinDate)) clause.append(" AND d.lastModifyDate >= :" + MinDate);
		if (searchCriteria.containsKey(MaxDate)) clause.append(" AND d.lastModifyDate <= :" + MaxDate);

		// Student Level
		if (searchCriteria.containsKey(SuitableStudent)) {
			clause.append(" AND (d.suitableStudent = 'Any' OR d.suitableStudent = '").append(searchCriteria.get(SuitableStudent)).append("')");
		}

		// Type
		if (searchCriteria.containsKey(Type)) {
			switch ((Dictation.DictationType) searchCriteria.get(Type)) {
				case Vocab:
					clause.append(" AND (d.article is null OR d.article = '')");
					break;
				case Article:
					clause.append(" AND d.article is not null AND d.article != ''");
					break;
			}
		}

		return clause.toString();
	}

	private void putSearchDictationParams(Query query, Map<DictationSearchCriteria, Object> searchCriteria) {
		if (searchCriteria.containsKey(MinAge)) query.setParameter(MinAge.toString(), searchCriteria.get(MinAge));
		if (searchCriteria.containsKey(MaxAge)) query.setParameter(MaxAge.toString(), searchCriteria.get(MaxAge));
		if (searchCriteria.containsKey(Title)) {
			String[] titles = ((String)searchCriteria.get(Title)).split(" ");
			for (int i=0; i < titles.length; i++) query.setParameter(Title.toString() + i, "%" + titles[i].toLowerCase() + "%");
		}
		if (searchCriteria.containsKey(Description)) {
			String[] descs = ((String)searchCriteria.get(Description)).split(" ");
			for (int i=0; i < descs.length; i++) query.setParameter(Description.toString() + i, "%" + descs[i].toLowerCase() + "%");
		}
		if (searchCriteria.containsKey(MinDate)) query.setParameter(MinDate.toString(), searchCriteria.get(MinDate));
		if (searchCriteria.containsKey(MaxDate)) query.setParameter(MaxDate.toString(), searchCriteria.get(MaxDate));
	}
}
