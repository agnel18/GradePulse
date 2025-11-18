package com.gradepulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    // Overall counts
    private Long totalStudents;
    private Long maleCount;
    private Long femaleCount;
    private Long otherGenderCount;
    
    // Category breakdown
    private Map<String, Long> categoryBreakdown;
    
    // Class breakdown
    private Map<String, Long> classBreakdown;
    
    // Fee status
    private Long paidCount;
    private Long pendingCount;
    private Long partialCount;
    
    // Attendance stats
    private Double averageAttendance;
    private Long aboveEightyPercent;
    private Long belowEightyPercent;
    
    // Recent activity
    private Long uploadsToday;
    private Long uploadsThisWeek;
    private Long uploadsThisMonth;
}
