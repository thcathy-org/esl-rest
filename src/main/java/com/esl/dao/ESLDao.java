package com.esl.dao;

import com.esl.entity.IAuditable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Transactional
public abstract class ESLDao<T> implements IESLDao<T> {
	protected Class<?> entityClass;

	@PersistenceContext
	protected EntityManager em;

	public ESLDao() {
		Class<?> clazz = getClass();
		while (!(clazz.getGenericSuperclass() instanceof ParameterizedType))
			clazz = (Class<?>) getClass().getGenericSuperclass();
		this.entityClass = (Class<?>) ((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public Object attachSession(Object o) {
		if (o == null) return null;
		return em.merge(o);
	}

	public void flush() {
		em.flush();
	}

	public T persist(T entity) {
		if (entity instanceof IAuditable) {
			((IAuditable)entity).setLastUpdatedDate(new Date());
		}
		em.persist(entity);
		return entity;
	}

	public void persistAll(Collection<? extends T> entities) {
		for (T entity : entities) {
			persist(entity);
		}
	}

	public void refresh(Object entity) {
		em.refresh(entity);
	}

	public T merge(T entity) {
		return (T) em.merge(entity);
	}

	public void delete(T entity) {
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
