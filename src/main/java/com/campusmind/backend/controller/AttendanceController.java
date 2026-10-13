
package com.campusmind.backend.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusmind.backend.entity.Attendance;
import com.campusmind.backend.entity.User;
import com.campusmind.backend.repository.AttendanceRepository;
import com.campusmind.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174"
})
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceController(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAttendance(
            @RequestParam(required = false) String date,
            Authentication authentication) {

        String email = authentication.getName();

        List<Attendance> records;

        if (date != null && !date.isBlank()) {
            LocalDate attendanceDate = LocalDate.parse(date);

            records = attendanceRepository
                    .findByOwnerEmailAndAttendanceDateOrderByStudentNameAsc(
                            email, attendanceDate);
        } else {
            records = attendanceRepository
                    .findByOwnerEmailOrderByAttendanceDateDesc(email);
        }

        return ResponseEntity.ok(records.stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String date,
            Authentication authentication) {

        String email = authentication.getName();
        LocalDate selectedDate = (date == null || date.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(date);

        long totalRecords =
                attendanceRepository.countByOwnerEmail(email);

        long todayRecords =
                attendanceRepository.countByOwnerEmailAndAttendanceDate(
                        email, selectedDate);

        long present =
                attendanceRepository
                        .countByOwnerEmailAndAttendanceDateAndStatusIgnoreCase(
                                email, selectedDate, "Present");

        long late =
                attendanceRepository
                        .countByOwnerEmailAndAttendanceDateAndStatusIgnoreCase(
                                email, selectedDate, "Late");

        long absent =
                attendanceRepository
                        .countByOwnerEmailAndAttendanceDateAndStatusIgnoreCase(
                                email, selectedDate, "Absent");

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", totalRecords);
        stats.put("date", selectedDate.toString());
        stats.put("todayRecords", todayRecords);
        stats.put("present", present);
        stats.put("late", late);
        stats.put("absent", absent);
        stats.put("attendanceRate", todayRecords == 0
                ? 0
                : Math.round(((double) (present + late) / todayRecords)
                        * 100));

        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<?> createAttendance(
            @RequestBody AttendanceRequest request,
            Authentication authentication) {

        if (request.getStudentId() == null
                || request.getStudentId().isBlank()
                || request.getStudentName() == null
                || request.getStudentName().isBlank()
                || request.getDepartment() == null
                || request.getDepartment().isBlank()
                || request.getAttendanceDate() == null
                || request.getStatus() == null
                || !List.of("Present", "Absent", "Late")
                        .contains(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please provide valid attendance details."));
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElse(null);

        if (owner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authenticated user was not found."));
        }

        Attendance attendance = new Attendance();
        attendance.setStudentId(request.getStudentId().trim());
        attendance.setStudentName(request.getStudentName().trim());
        attendance.setDepartment(request.getDepartment().trim());
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());
        attendance.setOwner(owner);

        Attendance saved = attendanceRepository.save(attendance);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Attendance attendance = attendanceRepository.findById(id)
                .filter(record -> record.getOwner() != null
                        && email.equalsIgnoreCase(
                                record.getOwner().getEmail()))
                .orElse(null);

        if (attendance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Attendance record not found."));
        }

        if (request.getStudentId() == null
                || request.getStudentId().isBlank()
                || request.getStudentName() == null
                || request.getStudentName().isBlank()
                || request.getDepartment() == null
                || request.getDepartment().isBlank()
                || request.getAttendanceDate() == null
                || request.getStatus() == null
                || !List.of("Present", "Absent", "Late")
                        .contains(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please provide valid attendance details."));
        }

        attendance.setStudentId(request.getStudentId().trim());
        attendance.setStudentName(request.getStudentName().trim());
        attendance.setDepartment(request.getDepartment().trim());
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());

        Attendance updated = attendanceRepository.save(attendance);

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        Attendance attendance = attendanceRepository.findById(id)
                .filter(record -> record.getOwner() != null
                        && email.equalsIgnoreCase(
                                record.getOwner().getEmail()))
                .orElse(null);

        if (attendance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Attendance record not found."));
        }

        attendanceRepository.delete(attendance);

        return ResponseEntity.ok(
                Map.of("message", "Attendance record deleted successfully."));
    }

    private Map<String, Object> toResponse(Attendance attendance) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", attendance.getId());
        result.put("studentId", attendance.getStudentId());
        result.put("studentName", attendance.getStudentName());
        result.put("department", attendance.getDepartment());
        result.put("attendanceDate",
                attendance.getAttendanceDate().toString());
        result.put("status", attendance.getStatus());
        result.put("createdAt", attendance.getCreatedAt());
        return result;
    }

    public static class AttendanceRequest {
        private String studentId;
        private String studentName;
        private String department;
        private LocalDate attendanceDate;
        private String status;

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public LocalDate getAttendanceDate() {
            return attendanceDate;
        }

        public void setAttendanceDate(LocalDate attendanceDate) {
            this.attendanceDate = attendanceDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
