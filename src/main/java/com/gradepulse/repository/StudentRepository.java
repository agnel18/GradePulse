package com.gradepulse.repository;

import com.gradepulse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}