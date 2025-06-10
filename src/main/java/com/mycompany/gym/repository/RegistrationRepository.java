package com.mycompany.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mycompany.gym.domain.Registration;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findAllByActivityId(Long activityId);
    List<Registration> findAllByUserId(Long userId);
}
