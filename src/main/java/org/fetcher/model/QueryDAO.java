package org.fetcher.model;

import org.hibernate.SessionFactory;

import java.util.List;

public class QueryDAO extends AbstractHibernateDao<Query> {

  public QueryDAO(SessionFactory sessionFactory) {
    super(sessionFactory, Query.class);
  }

  @SuppressWarnings("unchecked")
  public List<Query> findAllByJobId(int jobId) {
    return currentSession().createQuery("from Query where jobId = :id").setParameter("id", jobId).list();
  }

}
