package com.gradepulse.repository;

import com.gradepulse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // NEW: Check any contact
    boolean existsByFatherContactOrMotherContactOrGuardianContact(
        String fatherContact, 
        String motherContact, 
        String guardianContact
    );

    // Optional: Find by contact (for future WhatsApp)
    Student findByFatherContactOrMotherContactOrGuardianContact(
        String fatherContact, 
        String motherContact, 
        String guardianContact
    );

    // Find by studentId
    Student findByStudentId(String studentId);
    
    // Analytics queries
    long countByGender(String gender);
    
    @Query("SELECT s.category, COUNT(s) FROM Student s WHERE s.category IS NOT NULL GROUP BY s.category")
    List<Object[]> countByCategory();
    
    @Query("SELECT s.admissionClass, COUNT(s) FROM Student s WHERE s.admissionClass IS NOT NULL GROUP BY s.admissionClass")
    List<Object[]> countByClass();
    
    long countByFeeStatus(String feeStatus);
    
    @Query("SELECT AVG(s.attendancePercent) FROM Student s WHERE s.attendancePercent IS NOT NULL")
    Double getAverageAttendance();
    
    long countByAttendancePercentGreaterThanEqual(Double percent);
    long countByAttendancePercentLessThan(Double percent);
    
    // Attendance alert queries
    List<Student> findByAttendancePercentLessThan(Double percent);
    
    @Query("SELECT s FROM Student s WHERE s.attendancePercent >= :minPercent AND s.attendancePercent < :maxPercent")
    List<Student> findByAttendancePercentBetween(Double minPercent, Double maxPercent);
}