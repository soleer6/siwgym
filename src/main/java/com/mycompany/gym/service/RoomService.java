package com.mycompany.gym.service;

import com.mycompany.gym.domain.Room;
import com.mycompany.gym.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepo;

    public List<Room> findAll() {
        return roomRepo.findAll();
    }

    public Optional<Room> findById(Long id) {
        return roomRepo.findById(id);
    }

    public Room save(Room room) {
        return roomRepo.save(room);
    }

    public void deleteById(Long id) {
        roomRepo.deleteById(id);
    }
    public Optional<Room> findByName(String name) {
        return roomRepo.findByName(name);
    }
}
