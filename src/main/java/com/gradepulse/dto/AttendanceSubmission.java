package com.gradepulse.dto;

import com.gradepulse.model.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSubmission {
    private Long classSectionId;
    private LocalDate attendanceDate;
    private Map<Long, AttendanceStatus> studentAttendance; // studentId -> status
    private Map<Long, LocalTime> arrivalTimes; // studentId -> arrival time (for LATE status)
    private Map<Long, String> notes; // studentId -> notes
    private String markedBy;
}
