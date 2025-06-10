package com.mycompany.gym.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.gym.domain.Activity;
import com.mycompany.gym.domain.Registration;
import com.mycompany.gym.domain.User;
import com.mycompany.gym.repository.ActivityRepository;
import com.mycompany.gym.repository.RegistrationRepository;
import com.mycompany.gym.repository.SessionRepository;
import com.mycompany.gym.repository.UserRepository;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RegistrationRepository registrationRepo;

    @Autowired
    private SessionRepository sessionRepo;

    @Autowired
    private NotificationService notificationService;

    /** Returns all activities. */
    public List<Activity> findAll() {
        return activityRepo.findAll();
    }

    /** Finds an activity by its ID. */
    public Optional<Activity> findById(Long id) {
        return activityRepo.findById(id);
    }

    /** Saves (creates or updates) an activity. */
    public Activity save(Activity activity) {
        return activityRepo.save(activity);
    }

    /** Checks if an activity exists by ID. */
    public boolean existsById(Long id) {
        return activityRepo.existsById(id);
    }

    /** Deletes an activity by ID. */
    public void deleteById(Long id) {
        activityRepo.deleteById(id);
    }

    /**
     * Logic for a user to sign up for an activity.
     * 1) Retrieves Activity and User from their repositories.
     * 2) Avoids duplicates.
     * 3) Adds the User to the activity’s attendee list.
     * 4) Persists the relationship and sends a notification to the monitor.
     */
    @Transactional
    public boolean signupActivity(Long activityId, Long userId) {
        // 1) Retrieve Activity and User
        Activity activity = activityRepo.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Activity not found with id: " + activityId));

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found with id: " + userId));

        // 2) Check if the user is already signed up
        if (activity.getAttendees().contains(user)) {
            // Notify the user they are already enrolled
            String alreadyEnrolledMsg = String.format(
                "You are already enrolled in '%s'.", 
                activity.getName()
            );
            notificationService.createNotification(alreadyEnrolledMsg, user);
            return false;
        }

        // 3) Check if the capacity is already full
        int currentAttendeeCount = activity.getAttendees().size();
        int maximumCapacity     = activity.getMaxCapacity();
        if (currentAttendeeCount >= maximumCapacity) {
            // Notify the user that the activity is full
            String fullMsg = String.format(
                "Cannot enroll in '%s': capacity (%d/%d) is full.", 
                activity.getName(), 
                currentAttendeeCount, 
                maximumCapacity
            );
            notificationService.createNotification(fullMsg, user);
            return false;
        }

        // 4) Determine if, after adding this user, only ONE spot will remain
        boolean willBeSecondLast = (currentAttendeeCount == maximumCapacity - 2);
        // e.g. maxCapacity=10, currentAttendeeCount=8 → after enrolling, 9/10 → 1 spot left

        // 5) Determine if, after adding this user, the activity will become FULL
        boolean willBeLast = (currentAttendeeCount == maximumCapacity - 1);
        // e.g. maxCapacity=10, currentAttendeeCount=9 → after enrolling, 10/10 → now full

        // 6) Enroll the user
        activity.getAttendees().add(user);
        activityRepo.save(activity); // persist many-to-many relationship

        // 7) Notify the user of successful enrollment
        String userSuccessMsg = String.format(
            "You have successfully enrolled in '%s'. Capacity: %d/%d", 
            activity.getName(), 
            activity.getAttendees().size(), 
            activity.getMaxCapacity()
        );
        notificationService.createNotification(userSuccessMsg, user);

        // 8) Notify the monitor that a new user has enrolled
        User monitor = activity.getMonitor();
        if (monitor != null) {
            String monitorMsg = String.format(
                "User '%s' has enrolled in '%s'. Capacity: %d/%d",
                user.getUsername(),
                activity.getName(),
                activity.getAttendees().size(),
                activity.getMaxCapacity()
            );
            notificationService.createNotification(monitorMsg, monitor);
        }

        // 9) If only ONE spot remains after this enrollment → notify all NON-ENROLLED clients
        if (willBeSecondLast) {
            List<User> allClients = userRepo.findByRole("CLIENT");
            for (User client : allClients) {
                if (!activity.getAttendees().contains(client)) {
                    String oneSpotLeftMsg = String.format(
                        "Only ONE spot left in '%s'! Enroll before it's gone.",
                        activity.getName()
                    );
                    notificationService.createNotification(oneSpotLeftMsg, client);
                }
            }
        }

        // 10) If the activity has JUST BECOME FULL → notify ALL enrolled users and the monitor
        if (willBeLast) {
            // A) Notify every enrolled user
            for (User attendee : activity.getAttendees()) {
                String fullNotification = String.format(
                    "The activity '%s' is now FULL. Thank you for enrolling!",
                    activity.getName()
                );
                notificationService.createNotification(fullNotification, attendee);
            }
            // B) (Optional) Notify the monitor again that the activity is fully booked
            if (monitor != null) {
                String monitorFullMsg = String.format(
                    "The activity '%s' has reached full capacity (%d/%d).",
                    activity.getName(),
                    activity.getAttendees().size(),
                    activity.getMaxCapacity()
                );
                notificationService.createNotification(monitorFullMsg, monitor);
            }
        }
        Registration reg = new Registration(user, activity, activity.getPrice());
        registrationRepo.save(reg);

        return true;
    }

    @Transactional
    public Activity applyOfferToActivity(Long activityId, Double discountPct, LocalDateTime offerEndsAt) {
        Activity activity = activityRepo.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));

        activity.setDiscountPercentage(discountPct);
        activity.setOfferEndsAt(offerEndsAt);

        Activity updated = activityRepo.save(activity);

        // ------------- NOTIFICACIONES -------------
        // 1) Notificar al monitor asignado
        User monitor = activity.getMonitor();
        if (monitor != null) {
            String msgMonitor = String.format(
                "A new offer (%.0f%% off) has been created for your activity '%s'.",
                discountPct, activity.getName()
            );
            notificationService.createNotification(msgMonitor, monitor);
        }

        // 2) Notificar a TODOS los clientes NO INSCRITOS
        List<User> allClients = userRepo.findByRole("CLIENT");
        for (User client : allClients) {
            if (!activity.getAttendees().contains(client)) {
                String msgClient = String.format(
                    "Special offer! Activity '%s' now has %.0f%% off until %s. Check it out!",
                    activity.getName(),
                    discountPct,
                    offerEndsAt.toLocalDate().toString()
                );
                notificationService.createNotification(msgClient, client);
            }
        }

        return updated;
    }

      @Transactional
    public void deleteActivityWithCleanup(Long activityId) {
        Activity activity = activityRepo.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + activityId));

        // 1) Borrar de “registrations” (tabla intermedia)
        activityRepo.deleteAllRegistrationsNative(activityId);

        // 2) Borrar todas las sesiones de esta actividad
        sessionRepo.deleteAllByActivityId(activityId);

        // 3) Copiar monitor y asistentes actuales para notificar (si quieres avisarles)
        User monitor = activity.getMonitor();
        // (Si quieres notificar a los asistentes, lee primero la lista:)
        List<User> attendees = new ArrayList<>(activity.getAttendees());

        // 4) Borrar la actividad de la tabla “activities”
        activityRepo.deleteById(activityId);

        // 5) Notificar al monitor
        if (monitor != null) {
            String msgMonitor = String.format(
                "The activity '%s' (ID: %d) has been deleted by Admin.",
                activity.getName(), activityId
            );
            notificationService.createNotification(msgMonitor, monitor);
        }

        // 6) Notificar a cada cliente que estaba inscrito (si había alguno)
        for (User client : attendees) {
            String msgClient = String.format(
                "Your registration for '%s' has been cancelled because the activity was removed.",
                activity.getName()
            );
            notificationService.createNotification(msgClient, client);
        }
    }
}


