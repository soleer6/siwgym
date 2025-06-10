package com.mycompany.gym.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.gym.domain.Activity;
import com.mycompany.gym.domain.Room;
import com.mycompany.gym.domain.Session;
import com.mycompany.gym.domain.User;
import com.mycompany.gym.repository.SessionRepository;
import com.mycompany.gym.service.ActivityService;
import com.mycompany.gym.service.NotificationService;
import com.mycompany.gym.service.RoomService;
import com.mycompany.gym.service.UserService;
import static com.mycompany.gym.util.SecurityUtil.isAdmin;
import static com.mycompany.gym.util.SecurityUtil.isLoggedIn;

/**
 * Controlador de actividades. Cada método comprueba sesión manualmente:
 * - Si no hay userId en la sesión → devuelve 401.
 * - Solo usuarios “logueados” (con userId en sesión) pueden usar estos endpoints.
 */

 
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SessionRepository sessionRepo;


@PostMapping
public ResponseEntity<?> create(@RequestBody Map<String,Object> data,
                                HttpServletRequest request) {
    if (!isLoggedIn(request)) {
        return ResponseEntity.status(401).build();
    }

    // 1) Construir la entidad Activity
    Activity toSave = new Activity();
    toSave.setName((String) data.get("name"));
    toSave.setDescription((String) data.get("description"));
    toSave.setDateTime(LocalDateTime.parse((String) data.get("dateTime")));
    toSave.setMaxCapacity(((Number) data.get("maxCapacity")).intValue());
    toSave.setPrice(((Number) data.get("price")).doubleValue());
    toSave.setActive(true);  // asegúrate de marcarla activa

    // 2) Asignar monitor
    String monUser = (String) data.get("monitorUsername");
    if (monUser != null && !monUser.isEmpty()) {
        User mon = userService.findByUsername(monUser)
                      .orElseThrow(() -> new IllegalArgumentException("Monitor not found"));
        toSave.setMonitor(mon);
    }

    // 4) Procesar sesiones desde el JSON
    @SuppressWarnings("unchecked")
    List<Map<String,String>> sessionsData = (List<Map<String,String>>) data.get("sessions");
    if (sessionsData != null) {
        for (Map<String,String> s : sessionsData) {
            Session sess = new Session();
            sess.setStartTime(LocalDateTime.parse(s.get("startTime")));
            sess.setEndTime(LocalDateTime.parse(s.get("endTime")));
            String sessRoomName = s.get("roomName");
            if (sessRoomName != null && !sessRoomName.isEmpty()) {
                Room rs = roomService.findByName(sessRoomName)
                            .orElseThrow(() -> new IllegalArgumentException("Session room not found"));
                sess.setRoom(rs);
            }
            sess.setActivity(toSave);
            toSave.getSessions().add(sess);
        }
    }

    // 5) Guardar, notificar y devolver
    Activity created = activityService.save(toSave);

    String text = String.format("A new activity '%s' has been posted.", created.getName());
    userService.listAllClients()
               .forEach(c -> notificationService.createNotification(text, c));
    if (created.getMonitor() != null) {
        String msgMon = String.format(
            "You have been assigned as monitor for '%s' (ID: %d).",
            created.getName(), created.getId());
        notificationService.createNotification(msgMon, created.getMonitor());
    }

    URI uri = URI.create("/api/activities/" + created.getId());
    return ResponseEntity.created(uri).body(created);
}


    @GetMapping
    public ResponseEntity<?> findAll(HttpServletRequest request) {
        // Comprobamos sesión manualmente:
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        List<Activity> list = activityService.findAll();
        return ResponseEntity.ok(list);
    }

    /**
     * 2) Get a single activity by ID (para “View Info”).
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id, HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }
          return activityService.findById(id)
        .map(act -> {
            act.getSessions().size();
            return ResponseEntity.ok(act);
        })
        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/signup")
    public ResponseEntity<?> signUpUserToActivity(
        @PathVariable("id") Long activityId,
        @RequestParam("userId") Long userId,
        HttpServletRequest request) {

        // 1) Verificamos que el usuario esté logueado
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 2) Llamamos al método modificado que devuelve boolean
            boolean success = activityService.signupActivity(activityId, userId);

            if (success) {
                // Inscripción exitosa
                return ResponseEntity.ok().build();
            } else {
                // El service devolvió false → pudo ser porque ya estaba inscrito o porque la actividad está llena.
                // Podemos devolver 400 y un mensaje genérico. Si quieres mensajes más específicos,
                // podrías capturar en el service diferentes excepciones o devolver códigos de error más detallados.
                return ResponseEntity
                        .badRequest()
                        .body("Could not enroll in this activity.");
            }

        } catch (IllegalArgumentException e) {
            // Si activityId o userId no existen, el service lanza IllegalArgumentException
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        } catch (Exception e) {
            // Cualquier otra excepción inesperada
            return ResponseEntity
                    .status(500)
                    .body("Internal error while enrolling user.");
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Map<String,Object> data,
            HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }

        if(!isAdmin(request)){
            return ResponseEntity.status(401).build();
        }
        Activity existing = activityService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        

        existing.setName((String) data.get("name"));
        existing.setDescription((String) data.get("description"));
        existing.setDateTime(LocalDateTime.parse((String)data.get("dateTime")));
        existing.setMaxCapacity(((Number)data.get("maxCapacity")).intValue());
        existing.setPrice(((Number)data.get("price")).doubleValue());
        sessionRepo.deleteAllByActivityId(id);
        existing.setActive((boolean) data.getOrDefault("active", true));

        // NUEVA LÓGICA: asignar monitor por username
        String monitorUsername = (String) data.get("monitorUsername");
        if (monitorUsername != null && !monitorUsername.isEmpty()) {
            User mon = userService.findByUsername(monitorUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Monitor not found"));
            existing.setMonitor(mon);
        } else {
            existing.setMonitor(null);
        }

        // ========== Procesar sesiones ==========
        @SuppressWarnings("unchecked")
        List<Map<String,String>> sessionsData =
            (List<Map<String,String>>) data.get("sessions");

        // limpiamos las viejas
        existing.getSessions().clear();

        if (sessionsData != null) {
            for (Map<String,String> s : sessionsData) {
                Session sess = new Session();
                sess.setStartTime(LocalDateTime.parse(s.get("startTime")));
                sess.setEndTime(LocalDateTime.parse(s.get("endTime")));

                String sessRoomName = s.get("roomName");
                if (sessRoomName != null && !sessRoomName.isEmpty()) {
                    Room rs = roomService.findByName(sessRoomName)
                                .orElseThrow(() -> new IllegalArgumentException("Session room not found"));
                    sess.setRoom(rs);
                }

                sess.setActivity(existing);
                existing.getSessions().add(sess);
            }
        }
        Activity updated = activityService.save(existing);
        return ResponseEntity.ok(updated);
    }

@DeleteMapping("/admin/{id}")
public ResponseEntity<?> deleteActivity(
        @PathVariable("id") Long activityId,
        HttpServletRequest request) {

    if (!isLoggedIn(request)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (!isAdmin(request)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body("Only ADMIN can delete activities.");
    }

    try {
        activityService.deleteActivityWithCleanup(activityId);
        return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Unexpected error while deleting activity.");
    }
}

    @PutMapping("/admin/{id}/offer")
    public ResponseEntity<?> setOffer(
            @PathVariable("id") Long activityId,
            @RequestBody Map<String, Object> payload, // o un DTO específico
            HttpServletRequest request) {

        // 1) Verificar sesión y rol ADMIN
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Only ADMIN can set offers.");
        }

        // 2) Extraer parámetros del JSON: discountPercentage y offerEndsAt
        Double discountPct = ((Number) payload.get("discountPercentage")).doubleValue();
        String endsAtStr   = (String) payload.get("offerEndsAt");
        LocalDateTime endsAt = LocalDateTime.parse(endsAtStr);

        // 3) Delegar a ActivityService
        Activity updated = activityService.applyOfferToActivity(activityId, discountPct, endsAt);
        return ResponseEntity.ok(updated);
    }

    
}
