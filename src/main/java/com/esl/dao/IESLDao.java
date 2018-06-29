package com.esl.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface IESLDao<T> {
	public Object attachSession(Object o);
	public void flush();
	public T persist(T entity);
	public void persistAll(Collection<? extends T> entities);
	public void refresh(Object entity);
	public T merge(T entity);
	public void delete(Object entity);
	public void deleteAll(Collection<? extends Object> entities);
	public T get(Serializable id);
	public List<T> getAll();
}
