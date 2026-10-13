
package com.campusmind.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmind.backend.entity.Classroom;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);
}
