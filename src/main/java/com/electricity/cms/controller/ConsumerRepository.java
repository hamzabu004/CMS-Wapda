package com.electricity.cms.repository;

import com.electricity.cms.model.Consumer;
import com.electricity.cms.util.DatabaseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;
import java.util.UUID;

/**
 * Data-access layer for {@link Consumer} entities.
 */
public class ConsumerRepository {

    /**
     * Finds a consumer by their unique consumer ID string (e.g. "CONS-00123").
     */
    public Optional<Consumer> findByConsumerId(String consumer_id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Consumer> q = em.createQuery(
                    "SELECT c FROM Consumer c WHERE c.consumerId = :consumer_id", Consumer.class);
            q.setParameter("consumer_id", consumer_id);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Finds a consumer by their bill reference number.
     */
    public Optional<Consumer> findByBillReference(String bill_reference) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Consumer> q = em.createQuery(
                    "SELECT c FROM Consumer c WHERE c.billReference = :bill_ref", Consumer.class);
            q.setParameter("bill_ref", bill_reference);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Finds a consumer by their UUID primary key.
     */
    public Optional<Consumer> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(Consumer.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Persists a new consumer record.
     *
     * @throws RuntimeException if the database operation fails
     */
    public void save(Consumer consumer) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(consumer);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing consumer record.
     *
     * @throws RuntimeException if the database operation fails
     */
    public void update(Consumer consumer) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(consumer);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
