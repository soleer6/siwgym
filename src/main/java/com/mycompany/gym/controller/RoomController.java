package com.mycompany.gym.controller;

import com.mycompany.gym.domain.Room;
import com.mycompany.gym.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> listAll() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable Long id) {
        Optional<Room> opt = roomService.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody Room newRoom) {
        Room created = roomService.save(newRoom);
        URI uri = URI.create("/api/rooms/" + created.getId());
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody Room data) {
        Optional<Room> opt = roomService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Room existing = opt.get();
        existing.setName(data.getName());
        existing.setCapacity(data.getCapacity());
        Room updated = roomService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (roomService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
