package com.mycompany.gym;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.gym.domain.Activity;
import com.mycompany.gym.domain.Room;
import com.mycompany.gym.domain.Session;
import com.mycompany.gym.domain.User;
import com.mycompany.gym.repository.ActivityRepository;
import com.mycompany.gym.repository.RoomRepository;
import com.mycompany.gym.repository.SessionRepository;
import com.mycompany.gym.service.ActivityService;
import com.mycompany.gym.service.NotificationService;
import com.mycompany.gym.service.UserService;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final RoomRepository roomRepo;
    private final ActivityRepository activityRepo;
    private final SessionRepository sessionRepo;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserService userService,
                      RoomRepository roomRepo,
                      ActivityRepository activityRepo,
                      SessionRepository sessionRepo,
                      ActivityService activityService,
                      NotificationService notificationService,
                      PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roomRepo = roomRepo;
        this.activityRepo = activityRepo;
        this.sessionRepo = sessionRepo;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create or skip default users
        List<User> defaults = Arrays.asList(
            new User("admin",    "password", "ADMIN",   false),
            new User("monitor1", "password", "MONITOR", false),
            new User("monitor2", "password", "MONITOR", false),
            new User("monitor3", "password", "MONITOR", false),
            new User("client1",  "password", "CLIENT",  false),
            new User("client2",  "password", "CLIENT",  false),
            new User("client3",  "password", "CLIENT",  false)
        );
        for (User u : defaults) {
            if (userService.findByUsername(u.getUsername()).isEmpty()) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
                userService.save(u);
            }
        }

        // Create rooms if not exist
        List<Room> rooms = Arrays.asList(
            new Room("Room A", 20),
            new Room("Room B", 15),
            new Room("Room C", 25)
        );
        for (Room r : rooms) {
            if (roomRepo.findByName(r.getName()).isEmpty()) {
                roomRepo.save(r);
            }
        }

        // Create activities if not exist
        Optional<User> mon1 = userService.findByUsername("monitor1");
        Optional<User> mon2 = userService.findByUsername("monitor2");
        Optional<User> mon3 = userService.findByUsername("monitor3");

        if (mon1.isPresent() && activityRepo.findByName("Tiny Yoga").isEmpty()) {
            Activity tinyYoga = new Activity(
                "Tiny Yoga",
                "Quick intro to yoga, capacity very small",
                LocalDateTime.now().plusDays(1),
                2,
                5.0,
                mon1.get()
            );
            activityRepo.save(tinyYoga);
        }
        if (mon2.isPresent() && activityRepo.findByName("Pilates").isEmpty()) {
            Activity pilates = new Activity(
                "Pilates",
                "Core strength pilates session",
                LocalDateTime.now().plusDays(2),
                8,
                6.0,
                mon2.get()
            );
            activityRepo.save(pilates);
        }
        if (mon3.isPresent() && activityRepo.findByName("Spinning").isEmpty()) {
            Activity spinning = new Activity(
                "Spinning",
                "High-intensity spinning workout",
                LocalDateTime.now().plusDays(3),
                12,
                7.0,
                mon3.get()
            );
            activityRepo.save(spinning);
        }

        // Create sessions if not exist
        Room roomA = roomRepo.findByName("Room A").orElseThrow();
        Room roomB = roomRepo.findByName("Room B").orElseThrow();
        Room roomC = roomRepo.findByName("Room C").orElseThrow();

        Activity tinyYoga = activityRepo.findByName("Tiny Yoga").orElseThrow();
        if (sessionRepo.countByActivityId(tinyYoga.getId()) == 0) {
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                roomA,
                tinyYoga
            ));
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                roomA,
                tinyYoga
            ));
        }

        Activity pilates = activityRepo.findByName("Pilates").orElseThrow();
        if (sessionRepo.countByActivityId(pilates.getId()) == 0) {
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(2).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                roomB,
                pilates
            ));
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
                LocalDateTime.now().plusDays(2).withHour(12).withMinute(0),
                roomB,
                pilates
            ));
        }

        Activity spinning = activityRepo.findByName("Spinning").orElseThrow();
        if (sessionRepo.countByActivityId(spinning.getId()) == 0) {
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(3).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                roomC,
                spinning
            ));
            sessionRepo.save(new Session(
                LocalDateTime.now().plusDays(3).withHour(11).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(12).withMinute(0),
                roomC,
                spinning
            ));
        }

        // Sample signups
        userService.findByUsername("client1").ifPresent(c1 -> {
            activityService.signupActivity(tinyYoga.getId(), c1.getId());
            activityService.signupActivity(pilates.getId(), c1.getId());
        });
        userService.findByUsername("client2").ifPresent(c2 ->
            activityService.signupActivity(tinyYoga.getId(), c2.getId())
        );
    }
}
