package com.mycompany.gym.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycompany.gym.domain.Session;

// src/main/java/com/mycompany/gym/repository/SessionRepository.java
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.activity.id = :activityId")
    void deleteAllByActivityId(@Param("activityId") Long activityId);
    long countByActivityId(Long activityId);
}