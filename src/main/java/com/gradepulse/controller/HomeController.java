package com.gradepulse.controller;

import com.gradepulse.dto.DashboardStats;
import com.gradepulse.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStats stats = new DashboardStats();
        
        // Overall counts
        stats.setTotalStudents(studentRepository.count());
        stats.setMaleCount(studentRepository.countByGender("Male"));
        stats.setFemaleCount(studentRepository.countByGender("Female"));
        stats.setOtherGenderCount(studentRepository.countByGender("Other"));
        
        // Category breakdown
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        List<Object[]> categories = studentRepository.countByCategory();
        for (Object[] row : categories) {
            categoryMap.put((String) row[0], (Long) row[1]);
        }
        stats.setCategoryBreakdown(categoryMap);
        
        // Class breakdown
        Map<String, Long> classMap = new LinkedHashMap<>();
        List<Object[]> classes = studentRepository.countByClass();
        for (Object[] row : classes) {
            classMap.put((String) row[0], (Long) row[1]);
        }
        stats.setClassBreakdown(classMap);
        
        // Fee status
        stats.setPaidCount(studentRepository.countByFeeStatus("Paid"));
        stats.setPendingCount(studentRepository.countByFeeStatus("Pending"));
        stats.setPartialCount(studentRepository.countByFeeStatus("Partial"));
        
        // Attendance stats
        Double avgAttendance = studentRepository.getAverageAttendance();
        stats.setAverageAttendance(avgAttendance != null ? avgAttendance : 0.0);
        stats.setAboveEightyPercent(studentRepository.countByAttendancePercentGreaterThanEqual(80.0));
        stats.setBelowEightyPercent(studentRepository.countByAttendancePercentLessThan(80.0));
        
        // For now, set upload stats to 0 (we'll implement tracking later)
        stats.setUploadsToday(0L);
        stats.setUploadsThisWeek(0L);
        stats.setUploadsThisMonth(0L);
        
        model.addAttribute("stats", stats);
        return "dashboard";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}