package com.gradepulse.controller;

import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import com.gradepulse.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/attendance-alerts")
@RequiredArgsConstructor
@Slf4j
public class AttendanceAlertController {

    private final StudentRepository studentRepository;
    private final WhatsAppService whatsAppService;

    @GetMapping
    public String showAttendanceAlerts(Model model) {
        // Get students with low attendance (below 75%)
        List<Student> lowAttendanceStudents = studentRepository.findByAttendancePercentLessThan(75.0);
        
        // Get students with medium attendance (75-85%)
        List<Student> mediumAttendanceStudents = studentRepository
            .findByAttendancePercentBetween(75.0, 85.0);
        
        model.addAttribute("lowAttendanceStudents", lowAttendanceStudents);
        model.addAttribute("mediumAttendanceStudents", mediumAttendanceStudents);
        model.addAttribute("lowCount", lowAttendanceStudents.size());
        model.addAttribute("mediumCount", mediumAttendanceStudents.size());
        
        return "attendance-alerts";
    }

    @PostMapping("/send")
    public String sendAttendanceAlerts(
            @RequestParam(required = false) List<Long> studentIds,
            @RequestParam String alertType,
            RedirectAttributes redirectAttributes) {
        
        if (studentIds == null || studentIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one student");
            return "redirect:/attendance-alerts";
        }

        int sentCount = 0;
        int failedCount = 0;
        int rateLimitErrors = 0;
        
        for (Long studentId : studentIds) {
            if (studentId == null) continue;
            
            try {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) continue;

                String message = buildAttendanceMessage(student, alertType);
                
                // Send to father
                if (student.getFatherContact() != null && !student.getFatherContact().trim().isEmpty()) {
                    try {
                        whatsAppService.send(student.getFatherContact(), message);
                        sentCount++;
                        log.info("Sent alert to father: {}", student.getFatherContact());
                    } catch (Exception e) {
                        if (e.getMessage() != null && e.getMessage().contains("exceeded")) {
                            rateLimitErrors++;
                        }
                        log.warn("Failed to send to father {}: {}", student.getFatherContact(), e.getMessage());
                        failedCount++;
                    }
                }
                
                // Send to mother
                if (student.getMotherContact() != null && !student.getMotherContact().trim().isEmpty()) {
                    try {
                        whatsAppService.send(student.getMotherContact(), message);
                        sentCount++;
                        log.info("Sent alert to mother: {}", student.getMotherContact());
                    } catch (Exception e) {
                        if (e.getMessage() != null && e.getMessage().contains("exceeded")) {
                            rateLimitErrors++;
                        }
                        log.warn("Failed to send to mother {}: {}", student.getMotherContact(), e.getMessage());
                        failedCount++;
                    }
                }
                
                // Send to guardian
                if (student.getGuardianContact() != null && !student.getGuardianContact().trim().isEmpty()) {
                    try {
                        whatsAppService.send(student.getGuardianContact(), message);
                        sentCount++;
                        log.info("Sent alert to guardian: {}", student.getGuardianContact());
                    } catch (Exception e) {
                        if (e.getMessage() != null && e.getMessage().contains("exceeded")) {
                            rateLimitErrors++;
                        }
                        log.warn("Failed to send to guardian {}: {}", student.getGuardianContact(), e.getMessage());
                        failedCount++;
                    }
                }
                
                // Stop if hit rate limit
                if (rateLimitErrors > 0) {
                    log.warn("Hit Twilio rate limit, stopping to avoid further errors");
                    break;
                }
                
            } catch (Exception e) {
                log.error("Error processing student {}: {}", studentId, e.getMessage());
                failedCount++;
            }
        }
        
        if (sentCount > 0) {
            redirectAttributes.addFlashAttribute("success", 
                String.format("Successfully sent %d WhatsApp alerts", sentCount));
        }
        
        if (failedCount > 0) {
            String errorMsg = rateLimitErrors > 0 
                ? String.format("%d messages failed - Twilio daily limit (50) exceeded. Try again tomorrow.", failedCount)
                : String.format("%d messages failed to send (invalid numbers or network issues)", failedCount);
            redirectAttributes.addFlashAttribute("warning", errorMsg);
        }
        
        return "redirect:/attendance-alerts";
    }

    private String buildAttendanceMessage(Student student, String alertType) {
        double attendance = student.getAttendancePercent() != null ? student.getAttendancePercent() : 0.0;
        
        return switch (alertType) {
            case "critical" -> String.format(
                "🚨 URGENT ATTENDANCE ALERT\n\n" +
                "Student: %s\n" +
                "Class: %s\n" +
                "Current Attendance: %.1f%%\n\n" +
                "⚠️ This is critically low and requires immediate attention. " +
                "Please ensure regular attendance to avoid academic issues.\n\n" +
                "- GradePulse Team",
                student.getFullName(),
                student.getAdmissionClass(),
                attendance
            );
            case "warning" -> String.format(
                "⚠️ ATTENDANCE ALERT\n\n" +
                "Student: %s\n" +
                "Class: %s\n" +
                "Current Attendance: %.1f%%\n\n" +
                "Your child's attendance is below the recommended level. " +
                "Please help maintain regular attendance for better academic performance.\n\n" +
                "- GradePulse Team",
                student.getFullName(),
                student.getAdmissionClass(),
                attendance
            );
            case "reminder" -> String.format(
                "📋 ATTENDANCE REMINDER\n\n" +
                "Student: %s\n" +
                "Class: %s\n" +
                "Current Attendance: %.1f%%\n\n" +
                "This is a friendly reminder about attendance. " +
                "Regular attendance is important for academic success.\n\n" +
                "- GradePulse Team",
                student.getFullName(),
                student.getAdmissionClass(),
                attendance
            );
            default -> String.format(
                "📊 ATTENDANCE UPDATE\n\n" +
                "Student: %s\n" +
                "Current Attendance: %.1f%%\n\n" +
                "- GradePulse Team",
                student.getFullName(),
                attendance
            );
        };
    }
}
