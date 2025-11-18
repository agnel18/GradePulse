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
        
        for (Long studentId : studentIds) {
            try {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) continue;

                String message = buildAttendanceMessage(student, alertType);
                
                // Send to father
                if (student.getFatherContact() != null && student.getFatherContact().matches("\\d{10}")) {
                    try {
                        whatsAppService.send(student.getFatherContact(), message);
                        sentCount++;
                    } catch (Exception e) {
                        log.warn("Failed to send to father: {}", e.getMessage());
                        failedCount++;
                    }
                }
                
                // Send to mother
                if (student.getMotherContact() != null && student.getMotherContact().matches("\\d{10}")) {
                    try {
                        whatsAppService.send(student.getMotherContact(), message);
                        sentCount++;
                    } catch (Exception e) {
                        log.warn("Failed to send to mother: {}", e.getMessage());
                        failedCount++;
                    }
                }
                
                // Send to guardian
                if (student.getGuardianContact() != null && student.getGuardianContact().matches("\\d{10}")) {
                    try {
                        whatsAppService.send(student.getGuardianContact(), message);
                        sentCount++;
                    } catch (Exception e) {
                        log.warn("Failed to send to guardian: {}", e.getMessage());
                        failedCount++;
                    }
                }
                
                // Limit to 10 messages per request to avoid API limits
                if (sentCount >= 10) {
                    log.warn("Reached message limit of 10, stopping...");
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
            redirectAttributes.addFlashAttribute("warning", 
                String.format("%d messages failed to send (API limits or invalid numbers)", failedCount));
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
