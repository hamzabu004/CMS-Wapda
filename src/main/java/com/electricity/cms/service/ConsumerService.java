package com.electricity.cms.service;

import java.util.List;
import java.util.UUID;

import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.Person;
import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.repository.ConsumerRepository;
import com.electricity.cms.util.DatabaseUtil;

import jakarta.persistence.EntityManager;

public class ConsumerService {

    private final ConsumerRepository consumerRepository;

    public ConsumerService() {
        this(new ConsumerRepository());
    }

    public ConsumerService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }


    public Consumer getConsumerByUserId(UUID userId) {
        return consumerRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No consumer linked with this user."));
    }

    public List<Consumer> getAllConsumersByUserId(UUID userId) {
        return consumerRepository.findAllByUserId(userId);
    }

    public List<Consumer> getAllConsumersByPersonID(UUID personId) {
        return consumerRepository.findAllByPersonId(personId);
    }

    public Person getPersonByID(UUID personId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Person.class, personId);
        } finally {
            em.close();
        }
    }

     public User getUserByID(UUID userId) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(User.class, userId);
        } finally {
            em.close();
        }
    }

     public UserRole getUserRoleByID(UUID userId) {
        User user = getUserByID(userId);
        return user.getRole();
    }

    public UUID getPersonId(UUID userId)
    {
        return getUserByID(userId).getPerson().getId();
    }
}
