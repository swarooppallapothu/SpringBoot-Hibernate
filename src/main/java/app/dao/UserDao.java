package app.dao;

import app.models.User;
import java.util.List;

import javax.transaction.Transactional;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class UserDao extends AbstractDao<User> {

    public void save(User user) {
        super.create(user);
    }

    public void delete(User user) {
        super.delete(user.getId());
    }

    @SuppressWarnings("unchecked")
    public List<User> getAll() {

        @SuppressWarnings("unchecked")
        List<User> user = getCurrentSession().createCriteria(User.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
        return user;
    }

    public User getByEmail(String email) {
        return (User) getCurrentSession().createCriteria(User.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    public User getById(int id) {
        return super.get(id);
    }

    public void update(User user) {
//        getSession().update(user);
        return;
    }

} // class UserDao
