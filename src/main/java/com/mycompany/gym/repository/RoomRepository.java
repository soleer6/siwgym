package com.mycompany.gym.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycompany.gym.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
     Optional<Room> findByName(String name);
}
