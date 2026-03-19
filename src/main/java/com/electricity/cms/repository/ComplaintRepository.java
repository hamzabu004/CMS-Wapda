package com.electricity.cms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.ComplaintStatus;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;

public class ComplaintRepository {

    public Complaint save(Complaint complaint) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(complaint);
            em.getTransaction().commit();
            return complaint;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Complaint update(Complaint complaint) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint merged = em.merge(complaint);
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

    public Optional<Complaint> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(Complaint.class, id));
        } finally {
            em.close();
        }
    }

    public List<Complaint> findByConsumerId(UUID consumerId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.consumer.id = :consumerId ORDER BY c.lastUpdated DESC",
                    Complaint.class
                )
                .setParameter("consumerId", consumerId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> findByStatus(ComplaintStatus status) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.status = :status ORDER BY c.lastUpdated DESC",
                    Complaint.class
                )
                .setParameter("status", status)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> findUnassigned() {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c "+
					"WHERE c.status = 'PENDING' " + 
					"ORDER BY c.created_at ASC",
                    Complaint.class
                )
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> findByRegionId(UUID regionId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.consumer.region.id = :regionId ORDER BY c.lastUpdated DESC",
                    Complaint.class
                )
                .setParameter("regionId", regionId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> findEscalatedToManager() {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT c FROM Complaint c JOIN ComplaintStatusHistory h ON h.complaint = c " +
                    "WHERE LOWER(COALESCE(h.note, '')) LIKE :needle ORDER BY c.lastUpdated DESC",
                    Complaint.class
                )
                .setParameter("needle", "%manager%")
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> findByDateRange(LocalDateTime from, LocalDateTime to) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.createdAt BETWEEN :from AND :to ORDER BY c.createdAt DESC",
                    Complaint.class
                )
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint != null) {
                em.remove(complaint);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
