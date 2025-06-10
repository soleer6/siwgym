package com.mycompany.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mycompany.gym.domain.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
