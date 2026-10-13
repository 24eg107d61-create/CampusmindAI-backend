
package com.campusmind.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmind.backend.entity.Attendance;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findByOwnerEmailOrderByAttendanceDateDesc(
            String email);

    List<Attendance> findByOwnerEmailAndAttendanceDateOrderByStudentNameAsc(
            String email,
            LocalDate attendanceDate);

    long countByOwnerEmail(String email);

    long countByOwnerEmailAndAttendanceDate(
            String email,
            LocalDate attendanceDate);

    long countByOwnerEmailAndAttendanceDateAndStatusIgnoreCase(
            String email,
            LocalDate attendanceDate,
            String status);
}
