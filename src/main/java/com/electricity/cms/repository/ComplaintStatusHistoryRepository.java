package com.electricity.cms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.model.ComplaintStatusHistory;
import com.electricity.cms.model.TransferType;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;

public class ComplaintStatusHistoryRepository {

    public ComplaintStatusHistory save(ComplaintStatusHistory history) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(history);
            em.getTransaction().commit();
            return history;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<ComplaintStatusHistory> findByComplaintId(UUID complaintId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT h FROM ComplaintStatusHistory h WHERE h.complaint.id = :complaintId ORDER BY h.createdAt DESC",
                    ComplaintStatusHistory.class
                )
                .setParameter("complaintId", complaintId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<ComplaintStatusHistory> findLatestByComplaintId(UUID complaintId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<ComplaintStatusHistory> history = em.createQuery(
                    "SELECT h FROM ComplaintStatusHistory h WHERE h.complaint.id = :complaintId ORDER BY h.createdAt DESC",
                    ComplaintStatusHistory.class
                )
                .setParameter("complaintId", complaintId)
                .setMaxResults(1)
                .getResultList();
            return history.stream().findFirst();
        } finally {
            em.close();
        }
    }

    public List<UUID> findAssignedComplaintIdsForUser(UUID userId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT h.complaint.id FROM ComplaintStatusHistory h " +
                    "WHERE h.type = :type AND h.toUser.id = :userId",
                    UUID.class
                )
                .setParameter("type", TransferType.ASSIGNED)
                .setParameter("userId", userId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<UUID> findComplaintIdsRoutedToUser(UUID userId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT h.complaint.id FROM ComplaintStatusHistory h " +
                    "WHERE h.toUser.id = :userId AND h.type IN :types",
                    UUID.class
                )
                .setParameter("userId", userId)
                .setParameter("types", List.of(TransferType.ASSIGNED, TransferType.ESCALATED))
                .getResultList();
        } finally {
            em.close();
        }
    }
}
