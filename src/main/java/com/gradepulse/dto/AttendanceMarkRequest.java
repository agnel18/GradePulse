package com.gradepulse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkRequest {
    private String academicYear;
    private String board;
    private String stream;
    private String className;
    private String sectionName;
}
