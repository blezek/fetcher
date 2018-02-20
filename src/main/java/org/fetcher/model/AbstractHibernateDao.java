package org.fetcher.model;

import org.hibernate.SessionFactory;

import java.util.List;

import io.dropwizard.hibernate.AbstractDAO;

public class AbstractHibernateDao<T> extends AbstractDAO<T> {

  public AbstractHibernateDao(SessionFactory sessionFactory, Class<T> c) {
    super(sessionFactory);
    clazz = c;
  }

  private Class<T> clazz;

  public final void setClazz(Class<T> clazzToSet) {
    this.clazz = clazzToSet;
  }

  public T findOne(long id) {
    return currentSession().get(clazz, id);
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll() {
    return currentSession().createQuery("from " + clazz.getName()).list();
  }

  public void presist(T entity) {
    create(entity);
  }

  public void create(T entity) {
    currentSession().persist(entity);
  }

  public T merge(T entity) {
    return update(entity);
  }

  public T update(T entity) {
    return (T) currentSession().merge(entity);
  }

  public void delete(T entity) {
    currentSession().delete(entity);
  }

  public void deleteById(long entityId) {
    T entity = findOne(entityId);
    delete(entity);
  }

}