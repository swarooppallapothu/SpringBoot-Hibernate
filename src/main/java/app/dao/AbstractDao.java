package app.dao;

import app.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Provide methods for reading and writing objects from persistent storage.
 */
@Transactional
@Repository
public abstract class AbstractDao<T> {

    @PersistenceContext
    private EntityManager entityManager;
    private final Class<?> entityClass;
    private final Log log = LogFactory.getLog(AbstractDao.class);

    public AbstractDao() {
        this.entityClass = Util.getFirstGenericParameter(getClass());
    }

    protected Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Long getToTalCount() {
        Session session = getCurrentSession();
        Criteria criteria = session.createCriteria(entityClass);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public T get(int id) {
        Session session = getCurrentSession();
        return (T) session.get(entityClass, id);
    }

    @Transactional(readOnly = true)
    public Iterable<T> getAll() {
        Session session = getCurrentSession();
        @SuppressWarnings("unchecked")
        List<T> list = session.createCriteria(entityClass)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
        return list;
    }

    @Transactional(readOnly = true)
    public Iterable<T> getAll(Iterable<Integer> ids) {
        Session session = getCurrentSession();
        List<T> list = new ArrayList<T>();
        for (int id : ids) {
            @SuppressWarnings("unchecked")
            T entity = (T) session.get(entityClass, id);

            list.add(entity);
        }
        return list;
    }

    @Transactional(readOnly = true)
    public Iterable<T> getAll(int offset, int limit) {
        Session session = getCurrentSession();
        @SuppressWarnings("unchecked")
        Criteria criteria = session.createCriteria(entityClass);
        criteria.setProjection(Projections.distinct(Projections.property("id")));
        criteria.setFirstResult(offset * limit - limit);
        criteria.setMaxResults(limit);
        criteria.addOrder(Order.asc("id"));
        List uniqueSubList = criteria.list();
        criteria.setProjection(null);
        criteria.setFirstResult(0);
        criteria.setMaxResults(Integer.MAX_VALUE);
        criteria.add(Restrictions.in("id", uniqueSubList));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        List searchResults = criteria.list();

        return searchResults;
    }

    @Transactional
    public void create(T entity) {
        Session session = getCurrentSession();
        session.save(entity);
    }

    @Transactional
    public void update(T entity) {
        Session session = getCurrentSession();
        session.update(entity);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void delete(int id) {
        Session session = getCurrentSession();
        T entity = (T) session.load(entityClass, id);
        session.delete(entity);
    }

    ////////////////////////////////////////
    // patch support
    ////////////////////////////////////////
    private static final String PATCH_OPERATION_PERFORM_METHOD_NAME = "perform";

    /**
     * Return this exception (and HTTP response 409/CONFLICT) if the provided
     * JSON patch was created against a previous version of the object -- an
     * optimistic locking failure.
     */
    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Optimistic locking version out of date.")
    public static class ConcurrentModificationPatchException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ConcurrentModificationPatchException(Throwable e) {
            super(e);
        }
    }

    /**
     * Return this exception (and HTTP response 400/BAD_REQUEST) if the provided
     * JSON patch cannot be parsed.
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Cannot parse JSON patch.")
    public static class MalformedPatchException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MalformedPatchException(Throwable e) {
            super(e);
        }
    }

    /**
     * Return this exception (and HTTP response 404/NOT_FOUND) if the patch is
     * against an object that cannot be found.
     */
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Cannot find object.")
    public static class NotFoundPatchException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    /**
     * Return this exception (and HTTP response 422/UNPROCESSABLE_ENTITY) if the
     * patch cannot be applied for any reason, even though the JSON Patch format
     * is valid.
     */
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Cannot process patch.")
    public static class UnprocessablePatchException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnprocessablePatchException(Throwable e) {
            super(e);
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
