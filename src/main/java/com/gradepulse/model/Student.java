package com.gradepulse.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @Column(name = "apaar_id")
    private String apaarId;

    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    private String category;

    private String address;


    private Boolean photo;

    @Column(name = "previous_school_tc")
    private Boolean previousSchoolTc;

    @Column(name = "admission_class")
    private String admissionClass;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "enrollment_no", unique = true)
    private String enrollmentNo;

    @Column(name = "previous_marksheet")
    private Boolean previousMarksheet;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "allergies_conditions")
    private String allergiesConditions;

    private Boolean immunization;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "vision_check")
    private String visionCheck;

    @Column(name = "character_cert")
    private String characterCert;

    @Column(name = "fee_status")
    private String feeStatus;

    @Column(name = "attendance_percent")
    private Double attendancePercent;

    @Column(name = "udise_uploaded")
    private Boolean udiseUploaded;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "father_contact")
    private String fatherContact;

    @Column(name = "father_aadhaar")
    private String fatherAadhaar;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "mother_contact")
    private String motherContact;

    @Column(name = "mother_aadhaar")
    private String motherAadhaar;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_contact")
    private String guardianContact;

    @Column(name = "guardian_relation")
    private String guardianRelation;

    @Column(name = "guardian_aadhaar")
    private String guardianAadhaar;

    @Column(name = "family_status")
    private String familyStatus; //Two Fathers, Two Mothers, Single Father, Single Mother, Adoptive, Guardian, Orphan, Other

    @Column(name = "language_preference")
    private String languagePreference = "ENGLISH"; // Default

    // Constructors
    public Student() {}

    // Getters & Setters (only key ones shown — add rest as needed)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    // Add other getters/setters as needed
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getFatherContact() { return fatherContact; }
    public void setFatherContact(String fatherContact) { this.fatherContact = fatherContact; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getMotherContact() { return motherContact; }
    public void setMotherContact(String motherContact) { this.motherContact = motherContact; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianContact() { return guardianContact; }
    public void setGuardianContact(String guardianContact) { this.guardianContact = guardianContact; }

    public String getFamilyStatus() { return familyStatus; }
    public void setFamilyStatus(String familyStatus) { this.familyStatus = familyStatus; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getPhoto() { return photo; }
    public void setPhoto(Boolean photo) { this.photo = photo; }

    public Boolean getPreviousSchoolTc() { return previousSchoolTc; }
    public void setPreviousSchoolTc(Boolean previousSchoolTc) { this.previousSchoolTc = previousSchoolTc; }

    public String getAdmissionClass() { return admissionClass; }
    public void setAdmissionClass(String admissionClass) { this.admissionClass = admissionClass; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }

    public Boolean getPreviousMarksheet() { return previousMarksheet; }
    public void setPreviousMarksheet(Boolean previousMarksheet) { this.previousMarksheet = previousMarksheet; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAllergiesConditions() { return allergiesConditions; }
    public void setAllergiesConditions(String allergiesConditions) { this.allergiesConditions = allergiesConditions; }

    public Boolean getImmunization() { return immunization; }
    public void setImmunization(Boolean immunization) { this.immunization = immunization; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public String getVisionCheck() { return visionCheck; }
    public void setVisionCheck(String visionCheck) { this.visionCheck = visionCheck; }

    public String getCharacterCert() { return characterCert; }
    public void setCharacterCert(String characterCert) { this.characterCert = characterCert; }

    public String getFeeStatus() { return feeStatus; }
    public void setFeeStatus(String feeStatus) { this.feeStatus = feeStatus; }

    public Double getAttendancePercent() { return attendancePercent; }
    public void setAttendancePercent(Double attendancePercent) { this.attendancePercent = attendancePercent; }

    public Boolean getUdiseUploaded() { return udiseUploaded; }
    public void setUdiseUploaded(Boolean udiseUploaded) { this.udiseUploaded = udiseUploaded; }

    public String getFatherAadhaar() { return fatherAadhaar; }
    public void setFatherAadhaar(String fatherAadhaar) { this.fatherAadhaar = fatherAadhaar; }

    public String getMotherAadhaar() { return motherAadhaar; }
    public void setMotherAadhaar(String motherAadhaar) { this.motherAadhaar = motherAadhaar; }

    public String getGuardianRelation() { return guardianRelation; }
    public void setGuardianRelation(String guardianRelation) { this.guardianRelation = guardianRelation; }

    public String getGuardianAadhaar() { return guardianAadhaar; }
    public void setGuardianAadhaar(String guardianAadhaar) { this.guardianAadhaar = guardianAadhaar; }

    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }

}