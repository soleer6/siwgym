package com.mycompany.gym.service;

import com.mycompany.gym.domain.Notification;
import com.mycompany.gym.domain.User;
import com.mycompany.gym.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private UserService userService;

    public Notification createNotification(String description, User user) {
        Notification notification = new Notification(description, user);
        return notificationRepo.save(notification);
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationRepo.findByUserId(userId);
    }

    public Optional<Notification> findById(Long id) {
        return notificationRepo.findById(id);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        notification.setRead(true);
        return notificationRepo.save(notification);
    }
}
