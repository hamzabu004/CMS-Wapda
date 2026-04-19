package com.electricity.cms.repository;

import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.model.Region;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;

public class RegionRepository {

    public Optional<Region> findById(UUID id) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(Region.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Region> findFirstRegion() {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Region r", Region.class)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
