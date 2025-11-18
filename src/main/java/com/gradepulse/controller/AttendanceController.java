package com.gradepulse.controller;

import com.gradepulse.dto.AttendanceMarkRequest;
import com.gradepulse.dto.AttendanceSubmission;
import com.gradepulse.dto.StudentAttendanceDto;
import com.gradepulse.model.AttendanceStatus;
import com.gradepulse.model.ClassSection;
import com.gradepulse.repository.ClassSectionRepository;
import com.gradepulse.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final ClassSectionRepository classSectionRepository;
    private final AttendanceService attendanceService;

    @GetMapping("/mark")
    public String showAttendanceForm(Model model) {
        List<String> academicYears = classSectionRepository.findDistinctAcademicYears();
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("request", new AttendanceMarkRequest());
        return "attendance-form";
    }

    @PostMapping("/mark")
    public String showStudentList(@ModelAttribute AttendanceMarkRequest request, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        
        // Find the class section
        ClassSection classSection = classSectionRepository
            .findByAcademicYearAndBoardAndStreamAndClassNameAndSectionName(
                request.getAcademicYear(),
                request.getBoard(),
                request.getStream(),
                request.getClassName(),
                request.getSectionName()
            ).orElse(null);

        if (classSection == null) {
            redirectAttributes.addFlashAttribute("error", "Class section not found");
            return "redirect:/attendance/mark";
        }

        // Check if attendance already marked for today
        LocalDate today = LocalDate.now();
        if (attendanceService.isAttendanceMarkedForDate(classSection.getId(), today)) {
            redirectAttributes.addFlashAttribute("warning", 
                "Attendance already marked for " + classSection.getFullName() + " today");
        }

        // Get students
        List<StudentAttendanceDto> students = attendanceService.getStudentsForAttendance(classSection.getId());
        
        if (students.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", 
                "No students found for " + classSection.getFullName());
            return "redirect:/attendance/mark";
        }

        model.addAttribute("classSection", classSection);
        model.addAttribute("students", students);
        model.addAttribute("attendanceDate", today);
        model.addAttribute("attendanceStatuses", AttendanceStatus.values());
        
        return "mark-attendance";
    }

    @PostMapping("/submit")
    public String submitAttendance(
            @RequestParam Long classSectionId,
            @RequestParam(required = false) String attendanceDate,
            @RequestParam(required = false) String markedBy,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse attendance date
            LocalDate date = attendanceDate != null && !attendanceDate.isEmpty()
                ? LocalDate.parse(attendanceDate)
                : LocalDate.now();

            // Parse student attendance from form
            Map<Long, AttendanceStatus> studentAttendance = new HashMap<>();
            Map<Long, LocalTime> arrivalTimes = new HashMap<>();
            Map<Long, String> notes = new HashMap<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.startsWith("status_")) {
                    Long studentId = Long.parseLong(key.substring(7));
                    AttendanceStatus status = AttendanceStatus.valueOf(value);
                    studentAttendance.put(studentId, status);
                }
                else if (key.startsWith("arrivalTime_")) {
                    Long studentId = Long.parseLong(key.substring(12));
                    if (value != null && !value.isEmpty()) {
                        arrivalTimes.put(studentId, LocalTime.parse(value));
                    }
                }
                else if (key.startsWith("notes_")) {
                    Long studentId = Long.parseLong(key.substring(6));
                    if (value != null && !value.isEmpty()) {
                        notes.put(studentId, value);
                    }
                }
            }

            if (studentAttendance.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No attendance data provided");
                return "redirect:/attendance/mark";
            }

            // Create submission
            AttendanceSubmission submission = new AttendanceSubmission();
            submission.setClassSectionId(classSectionId);
            submission.setAttendanceDate(date);
            submission.setStudentAttendance(studentAttendance);
            submission.setArrivalTimes(arrivalTimes);
            submission.setNotes(notes);
            submission.setMarkedBy(markedBy != null && !markedBy.isEmpty() ? markedBy : "Teacher");

            // Submit attendance
            AttendanceService.AttendanceSubmissionResult result = attendanceService.submitAttendance(submission);

            // Prepare result message
            String successMsg = String.format(
                "✓ Attendance marked successfully for %d students. WhatsApp sent: %d", 
                result.getSuccessCount(), 
                result.getWhatsappSent()
            );

            if (result.getFailedCount() > 0) {
                String errorMsg = String.format(
                    "⚠ %d students failed: %s", 
                    result.getFailedCount(), 
                    String.join(", ", result.getErrors())
                );
                redirectAttributes.addFlashAttribute("warning", errorMsg);
            }

            redirectAttributes.addFlashAttribute("success", successMsg);
            redirectAttributes.addFlashAttribute("result", result);

            return "redirect:/attendance/success";

        } catch (Exception e) {
            log.error("Error submitting attendance: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit attendance: " + e.getMessage());
            return "redirect:/attendance/mark";
        }
    }

    @GetMapping("/success")
    public String showSuccess(Model model) {
        return "attendance-success";
    }

    // AJAX endpoints for cascading dropdowns
    @GetMapping("/api/boards")
    @ResponseBody
    public List<String> getBoards(@RequestParam String academicYear) {
        return classSectionRepository.findDistinctBoardsByAcademicYear(academicYear);
    }

    @GetMapping("/api/streams")
    @ResponseBody
    public List<String> getStreams(@RequestParam String academicYear, @RequestParam String board) {
        return classSectionRepository.findDistinctStreamsByAcademicYearAndBoard(academicYear, board);
    }

    @GetMapping("/api/classes")
    @ResponseBody
    public List<String> getClasses(@RequestParam String academicYear, 
                                   @RequestParam String board, 
                                   @RequestParam String stream) {
        return classSectionRepository.findDistinctClassNamesByAcademicYearBoardAndStream(
            academicYear, board, stream);
    }

    @GetMapping("/api/sections")
    @ResponseBody
    public List<ClassSection> getSections(@RequestParam String academicYear,
                                         @RequestParam String board,
                                         @RequestParam String stream,
                                         @RequestParam String className) {
        return classSectionRepository.findByAcademicYearAndBoardAndStreamAndClassNameAndIsActiveTrue(
            academicYear, board, stream, className);
    }
}
