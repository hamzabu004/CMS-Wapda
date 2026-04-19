package com.electricity.cms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserRepository {

    public User save(User user) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public User update(User user) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User merged = em.merge(user);
            em.getTransaction().commit();
            return merged;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

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

    public Optional<User> findByUsername(String username) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<User> users = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username",
                    User.class
                )
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
            return users.stream().findFirst();
        } finally {
            em.close();
        }
    }

    public boolean existsUserByCnic(String cnic) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(c) FROM Consumer c WHERE c.person.cnic = :cnic", Long.class)
                .setParameter("cnic", cnic)
                .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public List<User> findByRole(UserRole role) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.role = :role ORDER BY u.username",
                    User.class
                )
                .setParameter("role", role)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<User> findTechniciansByRegion(UUID regionId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.role = :role AND u.region.id = :regionId ORDER BY u.username",
                    User.class
                )
                .setParameter("role", UserRole.TECHNICIAN)
                .setParameter("regionId", regionId)
                .getResultList();
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
