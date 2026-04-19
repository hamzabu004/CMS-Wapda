package com.electricity.cms.repository;

import java.util.Optional;

import com.electricity.cms.model.Person;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class PersonRepository {

    public Optional<Person> findByCnic(String cnic) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.of(em.createQuery(
                "SELECT p FROM Person p WHERE p.cnic = :cnic", Person.class)
                .setParameter("cnic", cnic)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
