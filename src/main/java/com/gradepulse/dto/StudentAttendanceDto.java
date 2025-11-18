package com.gradepulse.dto;

import com.gradepulse.model.Student;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceDto {
    private Student student;
    private String lastAttendanceStatus;
    private String lastAttendanceDate;
    private Integer consecutiveAbsences;
    
    public StudentAttendanceDto(Student student) {
        this.student = student;
        this.consecutiveAbsences = 0;
    }
}
