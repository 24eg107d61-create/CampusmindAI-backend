
package com.campusmind.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.campusmind.backend.entity.Student;
import com.campusmind.backend.repository.StudentRepository;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174"
})
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Student ID already exists");
        }

        Student savedStudent = studentRepository.save(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent) {

        return studentRepository.findById(id)
                .map(student -> {
                    if (!student.getStudentId().equals(updatedStudent.getStudentId())
                            && studentRepository.existsByStudentId(
                                    updatedStudent.getStudentId())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Student ID already exists");
                    }

                    student.setStudentId(updatedStudent.getStudentId());
                    student.setName(updatedStudent.getName());
                    student.setDepartment(updatedStudent.getDepartment());
                    student.setYear(updatedStudent.getYear());
                    student.setEmail(updatedStudent.getEmail());
                    student.setPhone(updatedStudent.getPhone());

                    return ResponseEntity.ok(studentRepository.save(student));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (!studentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
