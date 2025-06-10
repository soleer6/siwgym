package com.mycompany.gym.controller;

import com.mycompany.gym.domain.Session;
import com.mycompany.gym.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<Session>> listAll() {
        return ResponseEntity.ok(sessionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getById(@PathVariable Long id) {
        Optional<Session> opt = sessionService.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Session> create(@RequestBody Session newSession) {
        Session created = sessionService.save(newSession);
        URI uri = URI.create("/api/sessions/" + created.getId());
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> update(@PathVariable Long id, @RequestBody Session data) {
        Optional<Session> opt = sessionService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Session existing = opt.get();
        existing.setStartTime(data.getStartTime());
        existing.setEndTime(data.getEndTime());
        existing.setRoom(data.getRoom());
        existing.setActivity(data.getActivity());
        Session updated = sessionService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (sessionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        sessionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
