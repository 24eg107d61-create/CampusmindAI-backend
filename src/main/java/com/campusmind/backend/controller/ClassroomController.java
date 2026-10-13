
package com.campusmind.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusmind.backend.entity.Classroom;
import com.campusmind.backend.repository.ClassroomRepository;

@RestController
@RequestMapping("/api/classrooms")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174"
})
public class ClassroomController {

    private final ClassroomRepository classroomRepository;

    public ClassroomController(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    // Get all classrooms
    @GetMapping
    public List<Classroom> getAllClassrooms() {
        System.out.println("CLASSROOM API CALLED");
        return classroomRepository.findAll();
    }

    // Add a classroom
    @PostMapping
    public ResponseEntity<?> addClassroom(
            @RequestBody Classroom classroom) {

        if (classroomRepository.existsByRoomNumber(
                classroom.getRoomNumber())) {
            return ResponseEntity.badRequest()
                    .body("Room number already exists");
        }

        Classroom saved = classroomRepository.save(classroom);
        return ResponseEntity.ok(saved);
    }

    // Update classroom details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassroom(
            @PathVariable Long id,
            @RequestBody Classroom updatedClassroom) {

        return classroomRepository.findById(id)
                .map(classroom -> {
                    classroom.setRoomNumber(
                            updatedClassroom.getRoomNumber());
                    classroom.setBuilding(
                            updatedClassroom.getBuilding());
                    classroom.setCapacity(
                            updatedClassroom.getCapacity());
                    classroom.setAvailability(
                            updatedClassroom.getAvailability());

                    return ResponseEntity.ok(
                            classroomRepository.save(classroom));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a classroom
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClassroom(
            @PathVariable Long id) {

        if (!classroomRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        classroomRepository.deleteById(id);

        return ResponseEntity.ok(
                "Classroom deleted successfully");
    }
}
