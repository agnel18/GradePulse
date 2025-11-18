package com.gradepulse.controller;

import com.google.gson.Gson;
import com.gradepulse.dto.StudentUploadDto;
import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import com.gradepulse.service.WhatsAppService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    private final Gson gson = new Gson();

    // === 1. Show upload page ===
    @GetMapping("/upload")
    public String uploadPage(Model model) {
        log.info("Accessing /upload page");
        model.addAttribute("totalStudents", studentRepository.count());
        return "upload";
    }

    // === 2. Handle upload → Preview ===
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        log.info("Starting file upload: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Empty file uploaded");
            model.addAttribute("error", "Please select a file");
            return "upload";
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        List<StudentUploadDto> previewList = new ArrayList<>();

        // Log headers
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            StringBuilder headers = new StringBuilder();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.append(getString(headerRow, i)).append(" | ");
            }
            log.info("Excel headers: {}", headers.toString());
        }

        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (row.getRowNum() == 0) continue; // skip header row only

            log.info("Processing row {} (Excel row {})", rowNum - 1, row.getRowNum());

            StudentUploadDto dto = new StudentUploadDto();
            dto.setStudentId(getString(row, 0));
            dto.setFullName(getString(row, 1));
            dto.setDateOfBirth(getDate(row, 2));
            dto.setGender(getString(row, 3));
            dto.setApaarId(getString(row, 4));
            dto.setAadhaarNumber(getString(row, 5));
            dto.setCategory(getString(row, 6));
            dto.setAddress(getString(row, 7));
            dto.setPhotoUrl(getString(row, 8));
            dto.setPreviousSchoolTcUrl(getString(row, 9));
            dto.setAdmissionClass(getString(row, 10));
            dto.setAdmissionDate(getDate(row, 11));
            dto.setEnrollmentNo(getString(row, 12));
            dto.setPreviousMarksheetUrl(getString(row, 13));
            dto.setBloodGroup(getString(row, 14));
            dto.setAllergiesConditions(getString(row, 15));
            dto.setImmunization(getBoolean(row, 16));
            dto.setHeightCm(getInt(row, 17));
            dto.setWeightKg(getInt(row, 18));
            dto.setVisionCheck(getString(row, 19));
            dto.setCharacterCertUrl(getString(row, 20));
            dto.setFeeStatus(getString(row, 21));
            dto.setAttendancePercent(getDouble(row, 22));
            dto.setUdiseUploaded(getBoolean(row, 23));

            // Family
            dto.setFatherName(getString(row, 24));
            dto.setFatherContact(getString(row, 25));
            dto.setFatherAadhaar(getString(row, 26));
            dto.setMotherName(getString(row, 27));
            dto.setMotherContact(getString(row, 28));
            dto.setMotherAadhaar(getString(row, 29));
            dto.setGuardianName(getString(row, 30));
            dto.setGuardianContact(getString(row, 31));
            dto.setGuardianRelation(getString(row, 32));
            dto.setGuardianAadhaar(getString(row, 33));
            dto.setFamilyStatus(getString(row, 34));
            dto.setLanguagePreference(getString(row, 35));

            // Validate
            validateDto(dto);

            // Compare with existing DB record
            if (dto.getStudentId() != null && !dto.getStudentId().isBlank()) {
                Student existing = studentRepository.findByStudentId(dto.getStudentId());
                if (existing != null) {
                    compareAndMarkChanges(dto, existing);
                }
            }

            log.info("Row {} → ID: {}, Name: {}, Valid: {}", rowNum - 2, dto.getStudentId(), dto.getFullName(), dto.isValid());

            previewList.add(dto);
        }

        log.info("===== PREVIEW DATA =====");
        log.info("Total rows processed: {}", previewList.size());
        for (int i = 0; i < previewList.size(); i++) {
            StudentUploadDto dto = previewList.get(i);
            log.info("Student {}: ID={}, Name={}, DOB={}, Father={}, Valid={}, Errors={}", 
                    i, dto.getStudentId(), dto.getFullName(), dto.getDateOfBirth(), 
                    dto.getFatherName(), dto.isValid(), dto.getErrors());
        }
        log.info("========================");

        model.addAttribute("students", previewList);
        model.addAttribute("totalImported", previewList.size());
        model.addAttribute("validCount", previewList.stream().filter(StudentUploadDto::isValid).count());

        log.info("Preview ready: {} total, {} valid", previewList.size(), model.getAttribute("validCount"));
        
        workbook.close();
        return "upload-preview";
    }

    // Compare all fields and mark changes in dto.changedFields and dto.oldValues
    private void compareAndMarkChanges(StudentUploadDto dto, Student existing) {
        // Compare each field, mark changedFields and oldValues if different
        if (!equalsObj(dto.getFullName(), existing.getFullName())) {
            dto.getChangedFields().put("fullName", true);
            dto.getOldValues().put("fullName", existing.getFullName());
        }
        if (!equalsObj(dto.getDateOfBirth(), existing.getDateOfBirth())) {
            dto.getChangedFields().put("dateOfBirth", true);
            dto.getOldValues().put("dateOfBirth", existing.getDateOfBirth());
        }
        if (!equalsObj(dto.getGender(), existing.getGender())) {
            dto.getChangedFields().put("gender", true);
            dto.getOldValues().put("gender", existing.getGender());
        }
        if (!equalsObj(dto.getApaarId(), existing.getApaarId())) {
            dto.getChangedFields().put("apaarId", true);
            dto.getOldValues().put("apaarId", existing.getApaarId());
        }
        if (!equalsObj(dto.getAadhaarNumber(), existing.getAadhaarNumber())) {
            dto.getChangedFields().put("aadhaarNumber", true);
            dto.getOldValues().put("aadhaarNumber", existing.getAadhaarNumber());
        }
        if (!equalsObj(dto.getCategory(), existing.getCategory())) {
            dto.getChangedFields().put("category", true);
            dto.getOldValues().put("category", existing.getCategory());
        }
        if (!equalsObj(dto.getAddress(), existing.getAddress())) {
            dto.getChangedFields().put("address", true);
            dto.getOldValues().put("address", existing.getAddress());
        }
        if (!equalsObj(dto.getPhotoUrl(), existing.getPhotoUrl())) {
            dto.getChangedFields().put("photoUrl", true);
            dto.getOldValues().put("photoUrl", existing.getPhotoUrl());
        }
        if (!equalsObj(dto.getPreviousSchoolTcUrl(), existing.getPreviousSchoolTcUrl())) {
            dto.getChangedFields().put("previousSchoolTcUrl", true);
            dto.getOldValues().put("previousSchoolTcUrl", existing.getPreviousSchoolTcUrl());
        }
        if (!equalsObj(dto.getAdmissionClass(), existing.getAdmissionClass())) {
            dto.getChangedFields().put("admissionClass", true);
            dto.getOldValues().put("admissionClass", existing.getAdmissionClass());
        }
        if (!equalsObj(dto.getAdmissionDate(), existing.getAdmissionDate())) {
            dto.getChangedFields().put("admissionDate", true);
            dto.getOldValues().put("admissionDate", existing.getAdmissionDate());
        }
        if (!equalsObj(dto.getEnrollmentNo(), existing.getEnrollmentNo())) {
            dto.getChangedFields().put("enrollmentNo", true);
            dto.getOldValues().put("enrollmentNo", existing.getEnrollmentNo());
        }
        if (!equalsObj(dto.getPreviousMarksheetUrl(), existing.getPreviousMarksheetUrl())) {
            dto.getChangedFields().put("previousMarksheetUrl", true);
            dto.getOldValues().put("previousMarksheetUrl", existing.getPreviousMarksheetUrl());
        }
        if (!equalsObj(dto.getBloodGroup(), existing.getBloodGroup())) {
            dto.getChangedFields().put("bloodGroup", true);
            dto.getOldValues().put("bloodGroup", existing.getBloodGroup());
        }
        if (!equalsObj(dto.getAllergiesConditions(), existing.getAllergiesConditions())) {
            dto.getChangedFields().put("allergiesConditions", true);
            dto.getOldValues().put("allergiesConditions", existing.getAllergiesConditions());
        }
        if (!equalsObj(dto.getImmunization(), existing.getImmunization())) {
            dto.getChangedFields().put("immunization", true);
            dto.getOldValues().put("immunization", existing.getImmunization());
        }
        if (!equalsObj(dto.getHeightCm(), existing.getHeightCm())) {
            dto.getChangedFields().put("heightCm", true);
            dto.getOldValues().put("heightCm", existing.getHeightCm());
        }
        if (!equalsObj(dto.getWeightKg(), existing.getWeightKg())) {
            dto.getChangedFields().put("weightKg", true);
            dto.getOldValues().put("weightKg", existing.getWeightKg());
        }
        if (!equalsObj(dto.getVisionCheck(), existing.getVisionCheck())) {
            dto.getChangedFields().put("visionCheck", true);
            dto.getOldValues().put("visionCheck", existing.getVisionCheck());
        }
        if (!equalsObj(dto.getCharacterCertUrl(), existing.getCharacterCertUrl())) {
            dto.getChangedFields().put("characterCertUrl", true);
            dto.getOldValues().put("characterCertUrl", existing.getCharacterCertUrl());
        }
        if (!equalsObj(dto.getAadhaarCardUrl(), existing.getAadhaarCardUrl())) {
            dto.getChangedFields().put("aadhaarCardUrl", true);
            dto.getOldValues().put("aadhaarCardUrl", existing.getAadhaarCardUrl());
        }
        if (!equalsObj(dto.getFeeStatus(), existing.getFeeStatus())) {
            dto.getChangedFields().put("feeStatus", true);
            dto.getOldValues().put("feeStatus", existing.getFeeStatus());
        }
        if (!equalsObj(dto.getAttendancePercent(), existing.getAttendancePercent())) {
            dto.getChangedFields().put("attendancePercent", true);
            dto.getOldValues().put("attendancePercent", existing.getAttendancePercent());
        }
        if (!equalsObj(dto.getUdiseUploaded(), existing.getUdiseUploaded())) {
            dto.getChangedFields().put("udiseUploaded", true);
            dto.getOldValues().put("udiseUploaded", existing.getUdiseUploaded());
        }
        if (!equalsObj(dto.getFatherName(), existing.getFatherName())) {
            dto.getChangedFields().put("fatherName", true);
            dto.getOldValues().put("fatherName", existing.getFatherName());
        }
        if (!equalsObj(dto.getFatherContact(), existing.getFatherContact())) {
            dto.getChangedFields().put("fatherContact", true);
            dto.getOldValues().put("fatherContact", existing.getFatherContact());
        }
        if (!equalsObj(dto.getFatherAadhaar(), existing.getFatherAadhaar())) {
            dto.getChangedFields().put("fatherAadhaar", true);
            dto.getOldValues().put("fatherAadhaar", existing.getFatherAadhaar());
        }
        if (!equalsObj(dto.getMotherName(), existing.getMotherName())) {
            dto.getChangedFields().put("motherName", true);
            dto.getOldValues().put("motherName", existing.getMotherName());
        }
        if (!equalsObj(dto.getMotherContact(), existing.getMotherContact())) {
            dto.getChangedFields().put("motherContact", true);
            dto.getOldValues().put("motherContact", existing.getMotherContact());
        }
        if (!equalsObj(dto.getMotherAadhaar(), existing.getMotherAadhaar())) {
            dto.getChangedFields().put("motherAadhaar", true);
            dto.getOldValues().put("motherAadhaar", existing.getMotherAadhaar());
        }
        if (!equalsObj(dto.getGuardianName(), existing.getGuardianName())) {
            dto.getChangedFields().put("guardianName", true);
            dto.getOldValues().put("guardianName", existing.getGuardianName());
        }
        if (!equalsObj(dto.getGuardianContact(), existing.getGuardianContact())) {
            dto.getChangedFields().put("guardianContact", true);
            dto.getOldValues().put("guardianContact", existing.getGuardianContact());
        }
        if (!equalsObj(dto.getGuardianRelation(), existing.getGuardianRelation())) {
            dto.getChangedFields().put("guardianRelation", true);
            dto.getOldValues().put("guardianRelation", existing.getGuardianRelation());
        }
        if (!equalsObj(dto.getGuardianAadhaar(), existing.getGuardianAadhaar())) {
            dto.getChangedFields().put("guardianAadhaar", true);
            dto.getOldValues().put("guardianAadhaar", existing.getGuardianAadhaar());
        }
        if (!equalsObj(dto.getFamilyStatus(), existing.getFamilyStatus())) {
            dto.getChangedFields().put("familyStatus", true);
            dto.getOldValues().put("familyStatus", existing.getFamilyStatus());
        }
        if (!equalsObj(dto.getLanguagePreference(), existing.getLanguagePreference())) {
            dto.getChangedFields().put("languagePreference", true);
            dto.getOldValues().put("languagePreference", existing.getLanguagePreference());
        }
    }

    private boolean equalsObj(Object a, Object b) {
        // Treat null and empty strings as equal
        String strA = (a == null) ? "" : a.toString().trim();
        String strB = (b == null) ? "" : b.toString().trim();
        return strA.equals(strB);
    }

    // === 3. Confirm & Save ===
    @PostMapping("/upload/confirm")
    public String confirmUpload(@RequestParam Map<String, String> allParams, Model model) {
        // Parse the students[index].field format from form
        Map<Integer, StudentUploadDto> dtoMap = new HashMap<>();
        
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("students[")) {
                int startIdx = key.indexOf('[') + 1;
                int endIdx = key.indexOf(']');
                int index = Integer.parseInt(key.substring(startIdx, endIdx));
                String field = key.substring(endIdx + 2); // skip "].
                
                StudentUploadDto dto = dtoMap.computeIfAbsent(index, k -> new StudentUploadDto());
                setDtoField(dto, field, entry.getValue());
            }
        }
        
        List<StudentUploadDto> dtos = new ArrayList<>(dtoMap.values());
        log.info("Confirming upload: {} DTOs received", dtos.size());

        List<Student> students = new ArrayList<>();
        int savedCount = 0;

        for (StudentUploadDto dto : dtos) {
            if (!dto.isValid()) {
                log.warn("Skipping invalid row: ID={}", dto.getStudentId());
                continue;
            }

            // Check if student exists - update instead of creating duplicate
            Student s = studentRepository.findByStudentId(dto.getStudentId());
            if (s == null) {
                s = new Student();
                log.info("Creating new student: ID={}", dto.getStudentId());
            } else {
                log.info("Updating existing student: ID={}", dto.getStudentId());
            }

            s.setStudentId(dto.getStudentId());
            s.setFullName(dto.getFullName());
            s.setDateOfBirth(dto.getDateOfBirth());
            s.setGender(dto.getGender());
            s.setApaarId(dto.getApaarId());
            s.setAadhaarNumber(dto.getAadhaarNumber());
            s.setCategory(dto.getCategory());
            s.setAddress(dto.getAddress());
            s.setPhotoUrl(dto.getPhotoUrl());
            s.setPreviousSchoolTcUrl(dto.getPreviousSchoolTcUrl());
            s.setAdmissionClass(dto.getAdmissionClass());
            s.setAdmissionDate(dto.getAdmissionDate());
            s.setEnrollmentNo(dto.getEnrollmentNo());
            s.setPreviousMarksheetUrl(dto.getPreviousMarksheetUrl());
            s.setBloodGroup(dto.getBloodGroup());
            s.setAllergiesConditions(dto.getAllergiesConditions());
            s.setImmunization(dto.getImmunization());
            s.setHeightCm(dto.getHeightCm());
            s.setWeightKg(dto.getWeightKg());
            s.setVisionCheck(dto.getVisionCheck());
            s.setCharacterCertUrl(dto.getCharacterCertUrl());
            s.setAadhaarCardUrl(dto.getAadhaarCardUrl());
            s.setFeeStatus(dto.getFeeStatus());
            s.setAttendancePercent(dto.getAttendancePercent());
            s.setUdiseUploaded(dto.getUdiseUploaded());

            s.setFatherName(dto.getFatherName());
            s.setFatherContact(dto.getFatherContact());
            s.setFatherAadhaar(dto.getFatherAadhaar());
            s.setMotherName(dto.getMotherName());
            s.setMotherContact(dto.getMotherContact());
            s.setMotherAadhaar(dto.getMotherAadhaar());
            s.setGuardianName(dto.getGuardianName());
            s.setGuardianContact(dto.getGuardianContact());
            s.setGuardianRelation(dto.getGuardianRelation());
            s.setGuardianAadhaar(dto.getGuardianAadhaar());
            s.setFamilyStatus(dto.getFamilyStatus());
            s.setLanguagePreference(dto.getLanguagePreference());

            if (s.getDynamicData() == null) {
                s.setDynamicData(gson.toJson(new java.util.HashMap<>()));
            }

            students.add(s);
            savedCount++;
            log.info("Mapped & queued for save: ID={}", s.getStudentId());
        }

        if (!students.isEmpty()) {
            studentRepository.saveAll(students);
            log.info("Successfully saved {} students", savedCount);
        }

        // Send WhatsApp (wrapped in try-catch to handle API limits)
        try {
            String welcome = "Welcome to GradePulse! Reply with:\n1 → English\n2 → हिंदी\n3 → தமிழ்\n4 → ಕನ್ನಡ";
            int sentCount = 0;
            for (Student s : students) {
                if (sentCount >= 10) break; // Limit to 10 messages per upload to avoid API limits
                
                if (s.getFatherContact() != null && s.getFatherContact().matches("\\d{10}")) {
                    whatsAppService.send(s.getFatherContact(), welcome);
                    log.info("WhatsApp sent to father: {}", s.getFatherContact());
                    sentCount++;
                }
                if (sentCount >= 10) break;
                
                if (s.getMotherContact() != null && s.getMotherContact().matches("\\d{10}")) {
                    whatsAppService.send(s.getMotherContact(), welcome);
                    log.info("WhatsApp sent to mother: {}", s.getMotherContact());
                    sentCount++;
                }
            }
            log.info("Successfully sent {} WhatsApp messages", sentCount);
        } catch (Exception e) {
            log.warn("WhatsApp sending failed (possibly due to API limits): {}", e.getMessage());
            // Continue execution - don't fail the upload if WhatsApp fails
        }

        model.addAttribute("message", "SUCCESS! " + savedCount + " students saved.");
        model.addAttribute("totalStudents", studentRepository.count());
        return "upload";
    }

    // === Helper Methods ===
    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private LocalDate getDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        
        // Handle numeric date cells (Excel date format)
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        
        // Handle string date cells
        String str = getString(row, col);
        if (str == null || str.trim().isEmpty()) return null;
        
        // Try multiple date formats
        String[] patterns = {
            "dd-MMM-yyyy",  // 15-Jan-2015
            "dd-MM-yyyy",   // 15-01-2015
            "yyyy-MM-dd",   // 2015-01-15
            "dd/MM/yyyy",   // 15/01/2015
            "MM/dd/yyyy"    // 01/15/2015
        };
        
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
                // Try next pattern
            }
        }
        
        log.warn("Could not parse date: {}", str);
        return null;
    }

    private Boolean getBoolean(Row row, int col) {
        String val = getString(row, col);
        if (val == null) return null;
        return val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true") || val.equals("1");
    }

    private Integer getInt(Row row, int col) {
        String val = getString(row, col);
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDouble(Row row, int col) {
        String val = getString(row, col);
        if (val == null) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateDto(StudentUploadDto dto) {
        log.info("Validating: ID={}, Name={}", dto.getStudentId(), dto.getFullName());

        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            dto.setValid(false);
            dto.getErrors().add("Student ID is required");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            dto.setValid(false);
            dto.getErrors().add("Full Name is required");
        }
        if ((dto.getFatherContact() == null || dto.getFatherContact().trim().isEmpty()) &&
            (dto.getMotherContact() == null || dto.getMotherContact().trim().isEmpty()) &&
            (dto.getGuardianContact() == null || dto.getGuardianContact().trim().isEmpty())) {
            dto.setValid(false);
            dto.getErrors().add("At least one contact (Father/Mother/Guardian) is required");
        }
        if (dto.getFatherContact() != null && !dto.getFatherContact().matches("\\d{10}")) {
            dto.setValid(false);
            dto.getErrors().add("Father Contact must be 10 digits");
        }
        if (dto.getMotherContact() != null && !dto.getMotherContact().matches("\\d{10}")) {
            dto.setValid(false);
            dto.getErrors().add("Mother Contact must be 10 digits");
        }

        log.info("Validation result: Valid={}, Errors={}", dto.isValid(), dto.getErrors());
    }
    
    private void setDtoField(StudentUploadDto dto, String field, String value) {
        if (value == null || value.trim().isEmpty()) return;

        try {
            switch (field) {
                case "studentId" -> dto.setStudentId(value);
                case "fullName" -> dto.setFullName(value);
                case "dateOfBirth" -> dto.setDateOfBirth(LocalDate.parse(value));
                case "gender" -> dto.setGender(value);
                case "apaarId" -> dto.setApaarId(value);
                case "aadhaarNumber" -> dto.setAadhaarNumber(value);
                case "category" -> dto.setCategory(value);
                case "address" -> dto.setAddress(value);
                case "photoUrl" -> dto.setPhotoUrl(value);
                case "previousSchoolTcUrl" -> dto.setPreviousSchoolTcUrl(value);
                case "admissionClass" -> dto.setAdmissionClass(value);
                case "admissionDate" -> dto.setAdmissionDate(LocalDate.parse(value));
                case "enrollmentNo" -> dto.setEnrollmentNo(value);
                case "previousMarksheetUrl" -> dto.setPreviousMarksheetUrl(value);
                case "bloodGroup" -> dto.setBloodGroup(value);
                case "allergiesConditions" -> dto.setAllergiesConditions(value);
                case "immunization" -> dto.setImmunization(parseBooleanValue(value));
                case "heightCm" -> dto.setHeightCm(parseIntegerValue(value));
                case "weightKg" -> dto.setWeightKg(parseIntegerValue(value));
                case "visionCheck" -> dto.setVisionCheck(value);
                case "characterCertUrl" -> dto.setCharacterCertUrl(value);
                case "aadhaarCardUrl" -> dto.setAadhaarCardUrl(value);
                case "feeStatus" -> dto.setFeeStatus(value);
                case "attendancePercent" -> dto.setAttendancePercent(Double.parseDouble(value));
                case "udiseUploaded" -> dto.setUdiseUploaded(parseBooleanValue(value));
                case "fatherName" -> dto.setFatherName(value);
                case "fatherContact" -> dto.setFatherContact(value);
                case "fatherAadhaar" -> dto.setFatherAadhaar(value);
                case "motherName" -> dto.setMotherName(value);
                case "motherContact" -> dto.setMotherContact(value);
                case "motherAadhaar" -> dto.setMotherAadhaar(value);
                case "guardianName" -> dto.setGuardianName(value);
                case "guardianContact" -> dto.setGuardianContact(value);
                case "guardianRelation" -> dto.setGuardianRelation(value);
                case "guardianAadhaar" -> dto.setGuardianAadhaar(value);
                case "familyStatus" -> dto.setFamilyStatus(value);
                case "languagePreference" -> dto.setLanguagePreference(value);
            }
        } catch (Exception e) {
            log.warn("Failed to set field {} = {}: {}", field, value, e.getMessage());
        }
    }

    // Helper to parse Yes/No/True/False to Boolean
    private Boolean parseBooleanValue(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase();
        if (v.equals("yes") || v.equals("true") || v.equals("1")) return true;
        if (v.equals("no") || v.equals("false") || v.equals("0")) return false;
        return null;
    }

    // Helper to parse integer from string, fallback to null
    private Integer parseIntegerValue(String value) {
        try {
            return (int) Math.round(Double.parseDouble(value));
        } catch (Exception e) {
            return null;
        }
    }
}