package com.electricity.cms.repository;
import com.electricity.cms.model.User;
import com.electricity.cms.util.DatabaseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Optional;
import java.util.UUID;
public class UserRepository {
    public Optional<User> findByEmail(String email) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    public Optional<User> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, id));
        } finally {
            em.close();
        }
    }
    public void save(User user) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    public void update(User user) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    public void deleteById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, id);
            if (u != null) em.remove(u);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
