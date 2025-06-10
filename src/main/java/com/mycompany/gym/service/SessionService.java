package com.mycompany.gym.service;

import com.mycompany.gym.domain.Session;
import com.mycompany.gym.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepo;

    public List<Session> findAll() {
        return sessionRepo.findAll();
    }

    public Optional<Session> findById(Long id) {
        return sessionRepo.findById(id);
    }

    public Session save(Session session) {
        return sessionRepo.save(session);
    }

    public void deleteById(Long id) {
        sessionRepo.deleteById(id);
    }
}
