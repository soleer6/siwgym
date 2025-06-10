package com.mycompany.gym.repository;



import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycompany.gym.domain.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // este método ejecuta un DELETE directo sobre la tabla intermedia “registrations”
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM registrations WHERE activity_id = :activityId", nativeQuery = true)
    void deleteAllRegistrationsNative(@Param("activityId") Long activityId);
    Optional <Activity> findByName(String name);
}
    

