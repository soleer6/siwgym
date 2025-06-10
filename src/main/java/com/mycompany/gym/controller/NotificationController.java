package com.mycompany.gym.controller;

import com.mycompany.gym.domain.Notification;
import com.mycompany.gym.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    @PostMapping("/{userId}/mark-read/{notifId}")
    public ResponseEntity<Notification> markRead(
            @PathVariable Long userId,
            @PathVariable Long notifId) {

        Optional<Notification> opt = notificationService.findById(notifId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Notification notif = opt.get();
        if (!notif.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body(null);
        }
        Notification updated = notificationService.markAsRead(notifId);
        return ResponseEntity.ok(updated);
    }
}
