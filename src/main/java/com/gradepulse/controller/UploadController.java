package com.gradepulse.controller;

import com.google.gson.Gson;
import com.gradepulse.dto.StudentUploadDto;
import com.gradepulse.model.ClassSection;
import com.gradepulse.model.FieldConfig;
import com.gradepulse.model.Student;
import com.gradepulse.repository.FieldConfigRepository;
import com.gradepulse.repository.StudentRepository;
import com.gradepulse.service.ClassSectionMappingService;
import com.gradepulse.service.WhatsAppService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private FieldConfigRepository fieldConfigRepository;

    @Autowired
    private ClassSectionMappingService classSectionMappingService;

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

        String filename = file.getOriginalFilename();
        if (filename == null) {
            model.addAttribute("error", "Invalid filename");
            return "upload";
        }

        // Determine file type and create appropriate workbook
        Workbook workbook;
        if (filename.toLowerCase().endsWith(".csv")) {
            log.info("Processing CSV file");
            workbook = convertCsvToWorkbook(file);
        } else if (filename.toLowerCase().endsWith(".xlsx")) {
            log.info("Processing XLSX file");
            workbook = new XSSFWorkbook(file.getInputStream());
        } else if (filename.toLowerCase().endsWith(".xls")) {
            log.info("Processing XLS file");
            workbook = new HSSFWorkbook(file.getInputStream());
        } else {
            model.addAttribute("error", "Unsupported file format. Please upload .xlsx, .xls, or .csv");
            return "upload";
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<StudentUploadDto> previewList = new ArrayList<>();

        // Build display name to field name mapping from database
        List<FieldConfig> allFields = fieldConfigRepository.findAll();
        Map<String, String> displayToFieldName = new HashMap<>();
        for (FieldConfig field : allFields) {
            displayToFieldName.put(field.getDisplayName().toLowerCase().trim(), field.getFieldName());
        }
        log.info("Loaded {} field mappings from database", displayToFieldName.size());

        // Read header row and build column index map
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            log.error("No header row found in Excel file");
            model.addAttribute("error", "Excel file must have a header row");
            workbook.close();
            return "upload";
        }

        Map<String, Integer> columnMap = buildColumnMap(headerRow, displayToFieldName);
        log.info("Built column map with {} columns: {}", columnMap.size(), columnMap.keySet());

        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (row.getRowNum() == 0) continue; // skip header row

            // Skip empty rows (check if Student ID and Full Name are both blank)
            String studentId = getStringByField(row, columnMap, "student_id");
            String fullName = getStringByField(row, columnMap, "full_name");
            
            if ((studentId == null || studentId.trim().isEmpty()) && 
                (fullName == null || fullName.trim().isEmpty())) {
                log.debug("Skipping empty row {} (Excel row {})", rowNum - 1, row.getRowNum());
                continue;
            }

            log.info("Processing row {} (Excel row {})", rowNum - 1, row.getRowNum());

            StudentUploadDto dto = new StudentUploadDto();
            
            // Map all fields dynamically using the column map
            dto.setStudentId(studentId);
            dto.setFullName(fullName);
            dto.setDateOfBirth(getDateByField(row, columnMap, "date_of_birth"));
            dto.setGender(getStringByField(row, columnMap, "gender"));
            dto.setApaarId(getStringByField(row, columnMap, "apaar_id"));
            dto.setAadhaarNumber(getStringByField(row, columnMap, "aadhaar_number"));
            dto.setCategory(getStringByField(row, columnMap, "category"));
            dto.setAddress(getStringByField(row, columnMap, "address"));
            dto.setPhotoUrl(getStringByField(row, columnMap, "photo_url"));
            dto.setPreviousSchoolTcUrl(getStringByField(row, columnMap, "previous_school_tc_url"));
            dto.setAdmissionClass(getStringByField(row, columnMap, "admission_class"));
            dto.setCurrentClass(getStringByField(row, columnMap, "current_class"));
            dto.setAdmissionDate(getDateByField(row, columnMap, "admission_date"));
            dto.setEnrollmentNo(getStringByField(row, columnMap, "enrollment_no"));
            dto.setPreviousMarksheetUrl(getStringByField(row, columnMap, "previous_marksheet_url"));
            dto.setBloodGroup(getStringByField(row, columnMap, "blood_group"));
            dto.setAllergiesConditions(getStringByField(row, columnMap, "allergies_conditions"));
            dto.setImmunization(getBooleanByField(row, columnMap, "immunization"));
            dto.setHeightCm(getIntByField(row, columnMap, "height_cm"));
            dto.setWeightKg(getIntByField(row, columnMap, "weight_kg"));
            dto.setVisionCheck(getStringByField(row, columnMap, "vision_check"));
            dto.setCharacterCertUrl(getStringByField(row, columnMap, "character_cert_url"));
            dto.setFeeStatus(getStringByField(row, columnMap, "fee_status"));
            dto.setAttendancePercent(getDoubleByField(row, columnMap, "attendance_percent"));
            dto.setUdiseUploaded(getBooleanByField(row, columnMap, "udise_uploaded"));

            // Family - normalize phone numbers
            dto.setFatherName(getStringByField(row, columnMap, "father_name"));
            dto.setFatherContact(normalizePhoneNumber(getStringByField(row, columnMap, "father_contact")));
            dto.setFatherAadhaar(getStringByField(row, columnMap, "father_aadhaar"));
            dto.setMotherName(getStringByField(row, columnMap, "mother_name"));
            dto.setMotherContact(normalizePhoneNumber(getStringByField(row, columnMap, "mother_contact")));
            dto.setMotherAadhaar(getStringByField(row, columnMap, "mother_aadhaar"));
            dto.setGuardianName(getStringByField(row, columnMap, "guardian_name"));
            
            String rawGuardianContact = getStringByField(row, columnMap, "guardian_contact");
            log.info("Row {}: Raw Guardian Contact = '{}', Normalized = '{}'", 
                     row.getRowNum(), rawGuardianContact, normalizePhoneNumber(rawGuardianContact));
            dto.setGuardianContact(normalizePhoneNumber(rawGuardianContact));
            
            dto.setGuardianRelation(getStringByField(row, columnMap, "guardian_relation"));
            dto.setGuardianAadhaar(getStringByField(row, columnMap, "guardian_aadhaar"));
            dto.setFamilyStatus(getStringByField(row, columnMap, "family_status"));
            dto.setLanguagePreference(getStringByField(row, columnMap, "language_preference"));

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
        
        // Pass active fields to preview page for dynamic column rendering
        List<FieldConfig> activeFields = fieldConfigRepository.findByActiveOrderBySortOrderAsc(true);
        model.addAttribute("activeFields", activeFields);
        
        // Create a set of active field names for easy checking in template
        Set<String> activeFieldNames = activeFields.stream()
            .map(FieldConfig::getFieldName)
            .collect(Collectors.toSet());
        model.addAttribute("activeFieldNames", activeFieldNames);
        
        // Map field names (snake_case DB) to DTO property names (camelCase)
        Map<String, String> fieldToPropMap = new HashMap<>();
        fieldToPropMap.put("student_id", "studentId");
        fieldToPropMap.put("full_name", "fullName");
        fieldToPropMap.put("date_of_birth", "dateOfBirth");
        fieldToPropMap.put("gender", "gender");
        fieldToPropMap.put("apaar_id", "apaarId");
        fieldToPropMap.put("aadhaar_number", "aadhaarNumber");
        fieldToPropMap.put("category", "category");
        fieldToPropMap.put("address", "address");
        fieldToPropMap.put("photo_url", "photoUrl");
        fieldToPropMap.put("previous_school_tc_url", "previousSchoolTcUrl");
        fieldToPropMap.put("admission_class", "admissionClass");
        fieldToPropMap.put("admission_date", "admissionDate");
        fieldToPropMap.put("enrollment_no", "enrollmentNo");
        fieldToPropMap.put("previous_marksheet_url", "previousMarksheetUrl");
        fieldToPropMap.put("blood_group", "bloodGroup");
        fieldToPropMap.put("allergies_conditions", "allergiesConditions");
        fieldToPropMap.put("immunization", "immunization");
        fieldToPropMap.put("height_cm", "heightCm");
        fieldToPropMap.put("weight_kg", "weightKg");
        fieldToPropMap.put("vision_check", "visionCheck");
        fieldToPropMap.put("character_cert_url", "characterCertUrl");
        fieldToPropMap.put("fee_status", "feeStatus");
        fieldToPropMap.put("attendance_percent", "attendancePercent");
        fieldToPropMap.put("udise_uploaded", "udiseUploaded");
        fieldToPropMap.put("father_name", "fatherName");
        fieldToPropMap.put("father_contact", "fatherContact");
        fieldToPropMap.put("father_aadhaar", "fatherAadhaar");
        fieldToPropMap.put("mother_name", "motherName");
        fieldToPropMap.put("mother_contact", "motherContact");
        fieldToPropMap.put("mother_aadhaar", "motherAadhaar");
        fieldToPropMap.put("guardian_name", "guardianName");
        fieldToPropMap.put("guardian_contact", "guardianContact");
        fieldToPropMap.put("guardian_relation", "guardianRelation");
        fieldToPropMap.put("guardian_aadhaar", "guardianAadhaar");
        fieldToPropMap.put("family_status", "familyStatus");
        fieldToPropMap.put("language_preference", "languagePreference");
        model.addAttribute("fieldToPropMap", fieldToPropMap);

        log.info("Preview ready: {} total, {} valid", previewList.size(), model.getAttribute("validCount"));
        
        workbook.close();
        return "upload-preview";
    }

    /**
     * Builds a map of field_name to column index by reading the Excel header row
     * and matching display names to database field names.
     */
    private Map<String, Integer> buildColumnMap(Row headerRow, Map<String, String> displayToFieldName) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String headerValue = getString(headerRow, i);
            if (headerValue == null || headerValue.trim().isEmpty()) {
                continue;
            }
            
            // Remove " *" suffix that indicates required fields in template
            String cleanHeader = headerValue.trim();
            if (cleanHeader.endsWith(" *")) {
                cleanHeader = cleanHeader.substring(0, cleanHeader.length() - 2).trim();
            }
            
            String normalizedHeader = cleanHeader.toLowerCase().trim();
            String fieldName = displayToFieldName.get(normalizedHeader);
            
            if (fieldName != null) {
                columnMap.put(fieldName, i);
                log.info("Mapped column {}: '{}' -> field '{}'", i, headerValue, fieldName);
            } else {
                log.warn("Unknown column header at index {}: '{}' (cleaned: '{}')", i, headerValue, cleanHeader);
            }
        }
        
        return columnMap;
    }

    // Helper methods to get values by field name instead of column index
    private String getStringByField(Row row, Map<String, Integer> columnMap, String fieldName) {
        Integer colIndex = columnMap.get(fieldName);
        if (colIndex == null) {
            return null;
        }
        return getString(row, colIndex);
    }

    private LocalDate getDateByField(Row row, Map<String, Integer> columnMap, String fieldName) {
        Integer colIndex = columnMap.get(fieldName);
        if (colIndex == null) {
            return null;
        }
        return getDate(row, colIndex);
    }

    private Boolean getBooleanByField(Row row, Map<String, Integer> columnMap, String fieldName) {
        Integer colIndex = columnMap.get(fieldName);
        if (colIndex == null) {
            return null;
        }
        return getBoolean(row, colIndex);
    }

    private Integer getIntByField(Row row, Map<String, Integer> columnMap, String fieldName) {
        Integer colIndex = columnMap.get(fieldName);
        if (colIndex == null) {
            return null;
        }
        return getInt(row, colIndex);
    }

    private Double getDoubleByField(Row row, Map<String, Integer> columnMap, String fieldName) {
        Integer colIndex = columnMap.get(fieldName);
        if (colIndex == null) {
            return null;
        }
        return getDouble(row, colIndex);
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
        if (!equalsObj(dto.getCurrentClass(), existing.getCurrentClass())) {
            dto.getChangedFields().put("currentClass", true);
            dto.getOldValues().put("currentClass", existing.getCurrentClass());
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
        // Extract academic year (default to current academic year)
        String academicYear = allParams.getOrDefault("academicYear", "2024-2025");
        log.info("Processing upload for academic year: {}", academicYear);
        
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
            
            // V10: Set granular class fields
            s.setStudentClass(dto.getStudentClass());
            s.setDivision(dto.getDivision());
            s.setSubDivision(dto.getSubDivision());
            
            // INTEGRATION FIX: Auto-link student to ClassSection using separate fields
            if (dto.getStudentClass() != null && !dto.getStudentClass().isBlank() &&
                dto.getSubDivision() != null && !dto.getSubDivision().isBlank()) {
                
                String board = allParams.getOrDefault("board", "CBSE");
                
                ClassSection classSection = classSectionMappingService.findOrCreateClassSection(
                    dto.getStudentClass(),
                    dto.getDivision(),
                    dto.getSubDivision(),
                    academicYear,
                    board
                );
                
                s.setClassSection(classSection);
                log.info("✓ Linked student {} to ClassSection: {}", 
                         dto.getStudentId(), 
                         classSection != null ? classSection.getFullName() : "none");
            } else {
                // Legacy fallback: Parse currentClass if separate fields not provided
                String classText = dto.getCurrentClass() != null && !dto.getCurrentClass().isBlank() 
                    ? dto.getCurrentClass() : dto.getAdmissionClass();
                
                if (classText != null && !classText.isBlank()) {
                    @SuppressWarnings("deprecation")
                    ClassSection classSection = classSectionMappingService.findOrCreateClassSection(
                        classText, academicYear);
                    s.setClassSection(classSection);
                } else {
                    s.setCurrentClass(dto.getAdmissionClass());
                }
            }
            
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
        
        // Validate phone numbers for WhatsApp format
        validatePhoneNumber(dto, dto.getFatherContact(), "Father Contact");
        validatePhoneNumber(dto, dto.getMotherContact(), "Mother Contact");
        validatePhoneNumber(dto, dto.getGuardianContact(), "Guardian Contact");

        log.info("Validation result: Valid={}, Errors={}", dto.isValid(), dto.getErrors());
    }
    
    private void validatePhoneNumber(StudentUploadDto dto, String phoneNumber, String fieldName) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Optional field
        }
        
        String phone = phoneNumber.trim();
        
        // Check for correct format: +[country code][number] (e.g., +971508714823, +919876543210)
        if (!phone.matches("^\\+\\d{10,15}$")) {
            dto.setValid(false);
            
            // Provide specific error messages based on common mistakes
            if (phone.contains("-") || phone.contains(" ")) {
                dto.getErrors().add(fieldName + " should not contain dashes or spaces. Use format: +971508714823");
            } else if (phone.startsWith("00")) {
                dto.getErrors().add(fieldName + " should start with + not 00. Example: +971508714823 (not 00971...)");
            } else if (!phone.startsWith("+")) {
                dto.getErrors().add(fieldName + " must start with country code. Example: +971508714823 or +919876543210");
            } else if (phone.length() < 11 || phone.length() > 16) {
                dto.getErrors().add(fieldName + " has invalid length. Format: +[country code][number] (e.g., +971508714823)");
            } else {
                dto.getErrors().add(fieldName + " has invalid format. Use: +971508714823 (no spaces/dashes)");
            }
        }
    }
    
    /**
     * Normalizes phone numbers by removing unwanted characters and adding + prefix if missing.
     * Handles Excel's tendency to strip the + sign from numbers.
     * Examples:
     * - "971508714823" -> "+971508714823"
     * - "+971508714823" -> "+971508714823"
     * - "00971508714823" -> "+971508714823"
     * - "+971-50-871-4823" -> "+971508714823"
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        
        String phone = phoneNumber.trim();
        
        // Remove all spaces, dashes, parentheses, dots
        phone = phone.replaceAll("[\\s\\-().]+", "");
        
        // Handle 00 prefix (convert to +)
        if (phone.startsWith("00")) {
            phone = "+" + phone.substring(2);
        }
        
        // If starts with + followed by digits, keep it
        if (phone.startsWith("+") && phone.substring(1).matches("^\\d{10,15}$")) {
            return phone;
        }
        
        // If it's just digits and looks like an international number (10-15 digits), add +
        if (phone.matches("^\\d{10,15}$")) {
            phone = "+" + phone;
        }
        
        return phone;
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
                case "currentClass" -> dto.setCurrentClass(value);
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

    /**
     * Convert CSV file to Apache POI Workbook for unified processing
     */
    private Workbook convertCsvToWorkbook(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int rowIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                Row row = sheet.createRow(rowIndex++);
                String[] cells = parseCsvLine(line);
                
                for (int i = 0; i < cells.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cells[i]);
                }
            }
        }
        
        log.info("Converted CSV to workbook with {} rows", sheet.getLastRowNum() + 1);
        return workbook;
    }

    /**
     * Parse CSV line handling quoted fields properly
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        
        return result.toArray(new String[0]);
    }
}