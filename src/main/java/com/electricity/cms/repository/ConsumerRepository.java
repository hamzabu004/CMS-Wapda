package com.electricity.cms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.model.Consumer;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;

public class ConsumerRepository {

    public Consumer save(Consumer consumer) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(consumer);
            em.getTransaction().commit();
            return consumer;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Consumer update(Consumer consumer) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Consumer merged = em.merge(consumer);
            em.getTransaction().commit();
            return merged;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Consumer> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(Consumer.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Consumer> findByUserId(UUID userId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<Consumer> result = em.createQuery(
                    "SELECT c FROM Consumer c " +
                    "JOIN c.user u " +
                    "JOIN c.person p " +
                    "JOIN c.region r " +
                    "WHERE u.id = :userId",
                    Consumer.class
                )
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();
            return result.stream().findFirst();
        } finally {
            em.close();
        }
    }

    public List<Consumer> findByRegionId(UUID regionId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Consumer c WHERE c.region.id = :regionId ORDER BY c.createdAt DESC",
                    Consumer.class
                )
                .setParameter("regionId", regionId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
