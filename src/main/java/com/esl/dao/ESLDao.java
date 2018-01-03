package com.esl.dao;

import com.esl.entity.IAuditable;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Transactional
public abstract class ESLDao<T> implements IESLDao<T> {
	protected Class<?> entityClass;

	@PersistenceContext	protected EntityManager em;

	/**
	 * Hibernate reattach object to session
	 * 
	 * @param practiceResult
	 */
	public Object attachSession(Object o) {
		if (o == null) return null;
		return em.merge(o);
	}

	public void flush() {
		em.flush();
	}

	public void persist(Object entity) {
		if (entity instanceof IAuditable) {
			((IAuditable)entity).setLastUpdatedDate(new Date());
		}
		em.persist(entity);
	}

	public void persistAll(Collection<? extends Object> entities) {
		for (Object entity : entities) {
			if (entity instanceof IAuditable) {
				((IAuditable)entity).setLastUpdatedDate(new Date());
			}
			em.persist(entity);
		}
	}

	public void refresh(Object entity) {
		em.refresh(entity);
	}

	@SuppressWarnings("unchecked")
	public T merge(T entity) {
		return (T) em.merge(entity);
	}

	public void delete(Object entity) {
		em.remove(entity);
	}
	public void deleteAll(Collection<? extends Object> entities) {
		for (Object entity : entities) {
			em.remove(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public T get(Serializable id) {
		return (T) em.find(entityClass, id);
	}

	@SuppressWarnings("unchecked")
	public List<T> getAll() {
		return em.createQuery("From " + entityClass.getName()).getResultList();
	}

}
