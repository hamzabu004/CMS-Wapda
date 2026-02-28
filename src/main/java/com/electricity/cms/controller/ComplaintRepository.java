package com.electricity.cms.repository;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.User;
import com.electricity.cms.util.DatabaseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data-access layer for {@link Complaint} entities.
 */
public class ComplaintRepository {

    private static final Logger LOGGER = Logger.getLogger(ComplaintRepository.class.getName());

    /**
     * Persists a new complaint.
     *
     * @throws RuntimeException if the database operation fails
     */
    public void save(Complaint complaint) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(complaint);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing complaint.
     *
     * @throws RuntimeException if the database operation fails
     */
    public void update(Complaint complaint) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(complaint);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a complaint by its UUID primary key.
     */
    public Optional<Complaint> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(Complaint.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Returns all complaints belonging to the person linked to the given user.
     * Returns an empty list if the user has no associated person or no complaints exist.
     */
    public List<Complaint> findByUser(User user) {
        if (user == null || user.getPerson() == null) {
            return Collections.emptyList();
        }
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Complaint> q = em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.person.id = :person_id ORDER BY c.createdAt DESC",
                    Complaint.class);
            q.setParameter("person_id", user.getPerson().getId());
            return q.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Returns all complaints in the system (admin use).
     */
    public List<Complaint> findAll() {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Complaint c ORDER BY c.createdAt DESC", Complaint.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a complaint by its UUID.
     */
    public void deleteById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint c = em.find(Complaint.class, id);
            if (c != null) em.remove(c);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
