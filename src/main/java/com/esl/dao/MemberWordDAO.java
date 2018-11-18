package com.esl.dao;

import com.esl.exception.IllegalParameterException;
import com.esl.model.Member;
import com.esl.model.MemberWord;
import com.esl.model.PhoneticQuestion;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

@Transactional
@Repository("memberWordDAO")
public class MemberWordDAO extends ESLDao<MemberWord> {
	private static Logger logger = LoggerFactory.getLogger(MemberWordDAO.class);
	public static int LEARNT_CORRECT_REQUIRE = 5;

	@Value("${MemberWordDAO.LearntCorrectRequire}") public void setLearntCorrectRequire(int learntCorrectRequire) { LEARNT_CORRECT_REQUIRE = learntCorrectRequire;}

	public MemberWordDAO() {}

	public MemberWord getMemberWordById(Long id) {
		return em.find(MemberWord.class, id);
	}

	public MemberWord getWord(Member member, PhoneticQuestion word) throws IllegalParameterException {
		if (member == null || word == null) throw new IllegalParameterException(new String[]{"member", "word"}, new Object[]{member, word});

		logger.info("getWord: input member[" + member.getUserId() + "], word[" + word.getWord() + "]");

		String query = "from MemberWord w where w.member = :member and w.word = :word";
		Query q = em.createQuery(query);
		List result = q.setParameter("member", member).setParameter("word", word).getResultList();

		if (result.size() > 0 ) return (MemberWord) result.get(0);
		else {
			logger.info("Do not find Member-Word [" + member.getUserId() + "," + word.getWord() + "]");
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<MemberWord> listLearntWords(Member member) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});
		logger.info("listLearntWords: input member[" + member.getUserId() + "]");

		String query = "from MemberWord w where w.member = :member and w.correctCount >= :count";
		return em.createQuery(query).setParameter("member", member).setParameter("count", LEARNT_CORRECT_REQUIRE).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<MemberWord> listWords(Member member) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});
		logger.info("listWords: input member[" + member.getUserId() + "]");

		String query = "from MemberWord w where w.member = :member";
		return em.createQuery(query).setParameter("member", member).getResultList();
	}

	public int totalWords(Member member) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});
		logger.info("totalWords: input member[" + member.getUserId() + "]");

		String queryStr = "SELECT COUNT(*) as counter FROM MemberWord w WHERE w.member = :member";
		Query query = em.createQuery(queryStr);
		query.setParameter("member", member);

		List results = query.getResultList();
		Long counter = (Long) results.get(0);
		logger.info("totalWords: return total:" + counter);
		return counter.intValue();
	}

	public void transit(MemberWord word) {
		MemberWord attachedWord = getMemberWordById(word.getId());
		em.detach(attachedWord);
		logger.info("transit: memberWord[" + word + "] is deleted");
	}

	public List<MemberWord> listRandomWords(Member member, int total, Collection<MemberWord> excludeWords) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});

		logger.info("listRandomWords: input member[" + member.getUserId() + "], total[" + total + "]");

		String queryString = "from MemberWord w where w.member = :member #isExclude# order by RAND()";

		// set "not in" list in sql
		if (excludeWords == null || excludeWords.size() < 1) {
			logger.info("listRandomWords: input excludeWords is null or empty");
			queryString = queryString.replace("#isExclude#", "");
		} else {
			logger.info("listRandomWords: input excludeWords.size[" + excludeWords.size() + "]");
			queryString = queryString.replace("#isExclude#", "and w not in (:excludeWords)");
		}
		logger.info("listRandomWords: queryString[" + queryString + "]");

		Query query = em.createQuery(queryString);
		query.setParameter("member", member);
		if (queryString.contains(":excludeWords")) query.setParameter("excludeWords", excludeWords);
		query.setMaxResults(total);

		return query.getResultList();
	}

	public int deleteWordsByCorrectCount(Member member, int count) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member", "count"}, new Object[]{member,count});
		logger.info("deleteWordsByCorrectCount: input member[" + member.getUserId() + "], count[" + count + "]");

		String queryString = "delete MemberWord w where w.member = :member and w.correctCount >= :count";
		Query query = em.createQuery(queryString);
		query.setParameter("member", member);
		query.setParameter("count", count);

		return query.executeUpdate();
	}

	public int deleteWordsByRate(Member member, double rate) throws IllegalParameterException {
		if (member == null) throw new IllegalParameterException(new String[]{"member", "rate"}, new Object[]{member,rate});
		logger.info("deleteWordsByRate: input member[" + member.getUserId() + "], rate[" + rate + "]");

		String queryString = "delete MemberWord w where w.member = :member and w.correctCount / w.trialCount >= :count";
		Query query = em.createQuery(queryString);
		query.setParameter("member", member);
		query.setParameter("rate", rate);

		return query.executeUpdate();
	}
}
