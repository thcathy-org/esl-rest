package com.esl.dao;

import com.esl.model.Member;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository("memberDAO")
public class MemberDAO extends ESLDao<Member> {
	private static final String GET_MEMBER_BY_USERID = "from Member m where m.userId = :userId";
	private static final String GET_MEMBER_BY_EMAIL = "from Member m where m.emailAddress = :emailAddress";
	
	private final Logger logger = LoggerFactory.getLogger(MemberDAO.class);

	public MemberDAO() {}

	public Member getMemberByUserID(String userId) {
		List result = em.createQuery(GET_MEMBER_BY_USERID).setParameter("userId", userId).getResultList();
		if (result.size() > 0)
			return (Member) result.get(0);
		else {
			logger.info("Do not find any member of userid:" + userId);
			return null;
		}
	}

	public Optional<Member> getMemberByEmail(String email) {
		List result = em.createQuery(GET_MEMBER_BY_EMAIL).setParameter("emailAddress", email).getResultList();
		if (result.size() > 0)
			return Optional.of((Member) result.get(0));
		else
			return Optional.empty();
	}

	@Transactional(readOnly = true)
	public Member getMemberByLoginedSessionID(String sessionId) {
		final String logPrefix = "getMemberByLoginedSessionID: ";
		logger.info(logPrefix + "START");

		final String queryStr = "FROM Member m WHERE m.loginedSessionId = :sessionId";
		javax.persistence.Query query = em.createQuery(queryStr);
		query.setParameter("sessionId", sessionId);
		query.setMaxResults(1);

		return (Member) query.getSingleResult();
	}

}
