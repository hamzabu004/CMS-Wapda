package com.electricity.cms.repository;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.util.DatabaseUtil;
import jakarta.persistence.EntityManager;

public class ComplaintRepository {

    public void save(Complaint complaint) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(complaint);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}