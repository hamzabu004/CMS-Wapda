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

    public void registerConsumer(Person person, Consumer consumer, String username, String email, String password) {
        EntityManager em = DatabaseUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            if (person == null || person.getId() == null) {
                throw new IllegalArgumentException("CNIC must already exist in person records before registration.");
            }

            Person managedPerson = em.find(Person.class, person.getId());
            if (managedPerson == null) {
                throw new IllegalArgumentException("Referenced CNIC record does not exist.");
            }

            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(password);
            user.setRole(UserRole.CUSTOMER);
            user.setPerson(managedPerson);
            user.setRegion(consumer.getRegion());
            em.persist(user);

            consumer.setPerson(managedPerson);
            consumer.setUser(user);
            em.persist(consumer);

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

    public Consumer getConsumerByUserId(UUID userId) {
        return consumerRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No consumer linked with this user."));
    }

    public List<Consumer> getAllConsumersByUserId(UUID userId) {
        return consumerRepository.findAllByUserId(userId);
    }
}
