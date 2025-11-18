package com.gradepulse.repository;

import com.gradepulse.model.AttendanceRecord;
import com.gradepulse.model.AttendanceStatus;
import com.gradepulse.model.ClassSection;
import com.gradepulse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByStudentAndAttendanceDate(Student student, LocalDate date);

    List<AttendanceRecord> findByClassSectionAndAttendanceDate(ClassSection classSection, LocalDate date);

    List<AttendanceRecord> findByStudentAndAttendanceDateBetween(Student student, LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findByClassSectionAndAttendanceDateBetween(ClassSection classSection, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student = ?1 AND ar.attendanceDate >= ?2 ORDER BY ar.attendanceDate DESC")
    List<AttendanceRecord> findRecentAttendanceByStudent(Student student, LocalDate fromDate);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student = ?1 AND ar.attendanceDate BETWEEN ?2 AND ?3")
    Long countByStudentAndDateRange(Student student, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student = ?1 AND ar.status = ?2 AND ar.attendanceDate BETWEEN ?3 AND ?4")
    Long countByStudentAndStatusAndDateRange(Student student, AttendanceStatus status, LocalDate startDate, LocalDate endDate);

    boolean existsByStudentAndAttendanceDate(Student student, LocalDate date);

    boolean existsByClassSectionAndAttendanceDate(ClassSection classSection, LocalDate date);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student = ?1 AND ar.status = 'ABSENT' AND ar.attendanceDate >= ?2 ORDER BY ar.attendanceDate DESC")
    List<AttendanceRecord> findConsecutiveAbsences(Student student, LocalDate fromDate);
}
