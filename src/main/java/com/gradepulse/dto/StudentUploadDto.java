package com.gradepulse.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class StudentUploadDto {
    private String studentId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String apaarId;
    private String aadhaarNumber;
    private String category;
    private String address;
    private String photoUrl;
    private String previousSchoolTcUrl;
    private String admissionClass;
    private LocalDate admissionDate;
    private String enrollmentNo;
    private String previousMarksheetUrl;
    private String bloodGroup;
    private String allergiesConditions;
    private Boolean immunization;
    private Integer heightCm;
    private Integer weightKg;
    private String visionCheck;
    private String characterCertUrl;
    private String aadhaarCardUrl;
    private String feeStatus;
    private Double attendancePercent;
    private Boolean udiseUploaded;

    // Family
    private String fatherName;
    private String fatherContact;
    private String fatherAadhaar;
    private String motherName;
    private String motherContact;
    private String motherAadhaar;
    private String guardianName;
    private String guardianContact;
    private String guardianRelation;
    private String guardianAadhaar;
    private String familyStatus;
    private String languagePreference;

    // Validation
    private boolean valid = true;
    private List<String> errors = new ArrayList<>();

    // Change tracking for preview
    private Map<String, Boolean> changedFields = new HashMap<>();
    private Map<String, Object> oldValues = new HashMap<>();
}