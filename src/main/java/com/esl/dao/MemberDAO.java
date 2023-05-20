package com.esl.dao;

import com.esl.entity.IAuditable;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository("memberDAO")
public class MemberDAO extends ESLDao<Member> {
	private static final String GET_MEMBER_BY_EMAIL = "from Member m where m.emailAddress = :emailAddress";
	
	private final Logger logger = LoggerFactory.getLogger(MemberDAO.class);

	public MemberDAO() {}

	@Cacheable("member")
	public Optional<Member> getMemberByEmail(String email) {
		logger.info("getMemberByEmail: {}", email);
		List result = em.createQuery(GET_MEMBER_BY_EMAIL).setParameter("emailAddress", email).getResultList();
		if (result.size() > 0)
			return Optional.of((Member) result.get(0));
		else
			return Optional.empty();
	}

	@Override
	@CachePut(value="member", key="#entity.emailAddress")
	public Member persist(Member entity) {
		if (entity instanceof IAuditable) {
			((IAuditable)entity).setLastUpdatedDate(new Date());
		}
		em.persist(entity);
		return entity;
	}
	@Override
	@CacheEvict(value="member", key="#entity.emailAddress")
	public void delete(Member entity) {
		super.delete(entity);
	}

}
