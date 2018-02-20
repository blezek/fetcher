package org.fetcher.model;

import org.hibernate.SessionFactory;

public class JobDAO extends AbstractHibernateDao<Job> {

  public JobDAO(SessionFactory sessionFactory) {
    super(sessionFactory, Job.class);
  }

}
