package com.gradepulse.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_sections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"academic_year", "board", "stream", "class_name", "section_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(nullable = false, length = 50)
    private String board;

    @Column(nullable = false, length = 50)
    private String stream;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "section_name", nullable = false, length = 20)
    private String sectionName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Helper method to get full display name
    public String getFullName() {
        return String.format("%s - %s %s (Section %s)", 
            board, stream, className, sectionName);
    }

    // Helper for dropdown display
    public String getDisplayName() {
        return String.format("%s %s - Section %s", className, stream, sectionName);
    }
}
