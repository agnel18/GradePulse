package com.gradepulse.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "attendance_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "marked_by", length = 100)
    private String markedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (markedAt == null) {
            markedAt = LocalDateTime.now();
        }
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
    }

    // Helper method to check if late
    public boolean isLate() {
        return status == AttendanceStatus.LATE;
    }

    // Helper method to check if absent
    public boolean isAbsent() {
        return status == AttendanceStatus.ABSENT;
    }
}
