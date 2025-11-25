package com.gradepulse.service;

import com.gradepulse.dto.AttendanceSubmission;
import com.gradepulse.dto.StudentAttendanceDto;
import com.gradepulse.model.*;
import com.gradepulse.repository.AttendanceRecordRepository;
import com.gradepulse.repository.ClassSectionRepository;
import com.gradepulse.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final ClassSectionRepository classSectionRepository;
    private final StudentRepository studentRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public AttendanceSubmissionResult submitAttendance(AttendanceSubmission submission) {
        Long classSectionId = submission.getClassSectionId();
        if (classSectionId == null) {
            throw new IllegalArgumentException("Class section ID cannot be null");
        }
        
        ClassSection classSection = classSectionRepository.findById(classSectionId)
            .orElseThrow(() -> new IllegalArgumentException("Class section not found"));

        LocalDate attendanceDate = submission.getAttendanceDate() != null 
            ? submission.getAttendanceDate() 
            : LocalDate.now();

        int successCount = 0;
        int failedCount = 0;
        int whatsappSent = 0;
        int whatsappFailed = 0;
        List<String> errors = new ArrayList<>();

        for (Map.Entry<Long, AttendanceStatus> entry : submission.getStudentAttendance().entrySet()) {
            Long studentId = entry.getKey();
            if (studentId == null) {
                errors.add("Student ID cannot be null");
                failedCount++;
                continue;
            }
            
            AttendanceStatus status = entry.getValue();

            try {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) {
                    errors.add("Student ID " + studentId + " not found");
                    failedCount++;
                    continue;
                }

                // Check for duplicate
                if (attendanceRepository.existsByStudentAndAttendanceDate(student, attendanceDate)) {
                    log.warn("Attendance already marked for {} on {}", student.getFullName(), attendanceDate);
                    errors.add(student.getFullName() + " - already marked for this date");
                    failedCount++;
                    continue;
                }

                // Create attendance record
                AttendanceRecord record = new AttendanceRecord();
                record.setStudent(student);
                record.setAttendanceDate(attendanceDate);
                record.setStatus(status);
                record.setClassSection(classSection);
                record.setAcademicYear(classSection.getAcademicYear());
                record.setMarkedBy(submission.getMarkedBy());

                // Set arrival time for LATE status
                if (status == AttendanceStatus.LATE && submission.getArrivalTimes() != null) {
                    LocalTime arrivalTime = submission.getArrivalTimes().get(studentId);
                    record.setArrivalTime(arrivalTime != null ? arrivalTime : LocalTime.now());
                }

                // Set notes if provided
                if (submission.getNotes() != null && submission.getNotes().containsKey(studentId)) {
                    record.setNotes(submission.getNotes().get(studentId));
                }

                attendanceRepository.save(record);
                successCount++;

                // Send WhatsApp notifications
                sendWhatsAppNotifications(student, status, record.getArrivalTime(), classSection);
                whatsappSent += 2; // Assuming sent to both parents

            } catch (Exception e) {
                log.error("Error marking attendance for student {}: {}", studentId, e.getMessage());
                errors.add("Student ID " + studentId + " - " + e.getMessage());
                failedCount++;
            }
        }

        return new AttendanceSubmissionResult(successCount, failedCount, whatsappSent, whatsappFailed, errors);
    }

    private void sendWhatsAppNotifications(Student student, AttendanceStatus status, 
                                          LocalTime arrivalTime, ClassSection classSection) {
        String message = buildWhatsAppMessage(student, status, arrivalTime, classSection);
        
        // Send to father
        if (student.getFatherContact() != null && !student.getFatherContact().trim().isEmpty()) {
            try {
                whatsAppService.send(student.getFatherContact(), message);
                log.info("Sent attendance notification to father: {}", student.getFatherContact());
            } catch (Exception e) {
                log.warn("Failed to send to father {}: {}", student.getFatherContact(), e.getMessage());
            }
        }

        // Send to mother
        if (student.getMotherContact() != null && !student.getMotherContact().trim().isEmpty()) {
            try {
                whatsAppService.send(student.getMotherContact(), message);
                log.info("Sent attendance notification to mother: {}", student.getMotherContact());
            } catch (Exception e) {
                log.warn("Failed to send to mother {}: {}", student.getMotherContact(), e.getMessage());
            }
        }
    }

    private String buildWhatsAppMessage(Student student, AttendanceStatus status, 
                                       LocalTime arrivalTime, ClassSection classSection) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String classInfo = classSection.getClassName() + " " + classSection.getSectionName();
        
        return switch (status) {
            case PRESENT -> String.format(
                "✓ ATTENDANCE UPDATE\n\n" +
                "%s (%s) is PRESENT today.\n\n" +
                "Date: %s\n" +
                "- GradePulse Team",
                student.getFullName(), classInfo, dateStr
            );
            case ABSENT -> String.format(
                "✗ ATTENDANCE ALERT\n\n" +
                "%s (%s) is ABSENT today.\n\n" +
                "Date: %s\n" +
                "Please contact the school if this is an error.\n\n" +
                "- GradePulse Team",
                student.getFullName(), classInfo, dateStr
            );
            case LATE -> {
                String timeStr = arrivalTime != null 
                    ? arrivalTime.format(DateTimeFormatter.ofPattern("hh:mm a")) 
                    : "N/A";
                yield String.format(
                    "⏰ ATTENDANCE UPDATE\n\n" +
                    "%s (%s) arrived LATE today at %s.\n\n" +
                    "Date: %s\n" +
                    "- GradePulse Team",
                    student.getFullName(), classInfo, timeStr, dateStr
                );
            }
            case HALF_DAY -> String.format(
                "½ ATTENDANCE UPDATE\n\n" +
                "%s (%s) is marked HALF DAY today.\n\n" +
                "Date: %s\n" +
                "- GradePulse Team",
                student.getFullName(), classInfo, dateStr
            );
        };
    }

    public List<StudentAttendanceDto> getStudentsForAttendance(Long classSectionId) {
        if (classSectionId == null) {
            throw new IllegalArgumentException("Class section ID cannot be null");
        }
        
        ClassSection classSection = classSectionRepository.findById(classSectionId)
            .orElseThrow(() -> new IllegalArgumentException("Class section not found"));

        // INTEGRATION FIX: Use FK relationship for primary query
        List<Student> students = studentRepository.findByClassSection(classSection);
        
        // Backward compatibility fallback: Try legacy string-based query if no results
        if (students.isEmpty()) {
            log.warn("No students found via FK for ClassSection ID {}. Trying legacy currentClass match for: {}", 
                     classSectionId, classSection.getFullName());
            students = studentRepository.findByCurrentClass(classSection.getFullName());
            
            // Also try simplified class name as last resort
            if (students.isEmpty()) {
                log.warn("No students via fullName. Trying className: {}", classSection.getClassName());
                students = studentRepository.findByCurrentClass(classSection.getClassName());
            }
        }

        List<StudentAttendanceDto> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Student student : students) {
            StudentAttendanceDto dto = new StudentAttendanceDto(student);
            
            // Get last attendance
            List<AttendanceRecord> recentRecords = attendanceRepository
                .findRecentAttendanceByStudent(student, today.minusDays(30));
            
            if (!recentRecords.isEmpty()) {
                AttendanceRecord lastRecord = recentRecords.get(0);
                dto.setLastAttendanceStatus(lastRecord.getStatus().getDisplayName());
                dto.setLastAttendanceDate(lastRecord.getAttendanceDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }

            // Calculate consecutive absences
            int consecutiveAbsences = calculateConsecutiveAbsences(student);
            dto.setConsecutiveAbsences(consecutiveAbsences);

            result.add(dto);
        }

        return result;
    }

    private int calculateConsecutiveAbsences(Student student) {
        List<AttendanceRecord> absences = attendanceRepository
            .findConsecutiveAbsences(student, LocalDate.now().minusDays(30));
        
        int count = 0;
        LocalDate expectedDate = LocalDate.now().minusDays(1);
        
        for (AttendanceRecord record : absences) {
            if (record.getAttendanceDate().equals(expectedDate) || 
                record.getAttendanceDate().equals(expectedDate.minusDays(1)) ||
                record.getAttendanceDate().equals(expectedDate.minusDays(2))) {
                count++;
                expectedDate = record.getAttendanceDate().minusDays(1);
            } else {
                break;
            }
        }
        
        return count;
    }

    public boolean isAttendanceMarkedForDate(Long classSectionId, LocalDate date) {
        if (classSectionId == null) {
            return false;
        }
        
        ClassSection classSection = classSectionRepository.findById(classSectionId).orElse(null);
        if (classSection == null) {
            return false;
        }
        return attendanceRepository.existsByClassSectionAndAttendanceDate(classSection, date);
    }

    // Inner class for result
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AttendanceSubmissionResult {
        private int successCount;
        private int failedCount;
        private int whatsappSent;
        private int whatsappFailed;
        private List<String> errors;
    }
}
