package com.electricity.cms.repository;

import com.electricity.cms.model.ComplaintMessage;
import com.electricity.cms.util.DatabaseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class ComplaintMessageRepository {

    public void save(ComplaintMessage message) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(message);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<ComplaintMessage> findByComplaintId(UUID complaintId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ComplaintMessage> q = em.createQuery(
                    "SELECT m FROM ComplaintMessage m WHERE m.complaintId = :cid ORDER BY m.createdAt ASC",
                    ComplaintMessage.class
            );
            q.setParameter("cid", complaintId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}