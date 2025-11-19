package com.gradepulse.controller;

import com.gradepulse.model.FieldConfig;
import com.gradepulse.repository.FieldConfigRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class FieldConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(FieldConfigController.class);
    
    @Autowired
    private FieldConfigRepository fieldConfigRepository;

    @GetMapping("/fields")
    public String listFields(Model model) {
        List<FieldConfig> fields = fieldConfigRepository.findAllByOrderBySortOrderAsc();
        model.addAttribute("fields", fields);
        model.addAttribute("totalFields", fields.size());
        model.addAttribute("activeFields", fields.stream().filter(FieldConfig::getActive).count());
        log.info("Loaded {} fields", fields.size());
        return "fields";
    }

    @PostMapping("/fields/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addField(@RequestBody FieldConfig field) {
        log.info("Adding new field: {}", field.getFieldName());
        
        // Validate
        if (fieldConfigRepository.existsByFieldName(field.getFieldName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Field name already exists"));
        }
        
        // Set default sort order (last)
        Integer maxOrder = fieldConfigRepository.findMaxSortOrder();
        field.setSortOrder(maxOrder == null ? 0 : maxOrder + 1);
        
        FieldConfig saved = fieldConfigRepository.save(field);
        log.info("Field saved with ID: {}", saved.getId());
        
        return ResponseEntity.ok(Map.of("success", true, "field", saved));
    }

    @PostMapping("/fields/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateField(@PathVariable Long id, @RequestBody FieldConfig updatedField) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        
        log.info("Updating field ID: {}", id);
        
        return fieldConfigRepository.findById(id).map(field -> {
            if (field == null) {
                return ResponseEntity.notFound().<Map<String, Object>>build();
            }
            field.setDisplayName(updatedField.getDisplayName());
            field.setFieldType(updatedField.getFieldType());
            field.setRequired(updatedField.getRequired());
            field.setActive(updatedField.getActive());
            
            FieldConfig saved = fieldConfigRepository.save(field);
            log.info("Field updated: {}", saved.getFieldName());
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "field", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleField(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        return fieldConfigRepository.findById(id).map(field -> {
            field.setActive(!field.getActive());
            fieldConfigRepository.save(field);
            log.info("Field {} toggled to {}", field.getFieldName(), field.getActive() ? "active" : "inactive");
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "active", field.getActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteField(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        
        return fieldConfigRepository.findById(id).map(field -> {
            if (field == null) {
                return ResponseEntity.notFound().<Map<String, Object>>build();
            }
            fieldConfigRepository.delete(field);
            log.info("Field deleted: {}", field.getFieldName());
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/reorder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reorderFields(@RequestBody List<Long> fieldIds) {
        log.info("Reordering {} fields", fieldIds.size());
        
        for (int i = 0; i < fieldIds.size(); i++) {
            final int sortOrder = i;
            Long fieldId = fieldIds.get(i);
            if (fieldId != null) {
                fieldConfigRepository.findById(fieldId).ifPresent(field -> {
                    field.setSortOrder(sortOrder);
                    fieldConfigRepository.save(field);
                });
            }
        }
        
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    /**
     * Generate dynamic Excel template based on active field configuration
     * Core fields (student_id, full_name, admission_class, current_class) are always included first
     * Then adds all active fields from field_config table in sort order
     */
    @GetMapping("/template.xlsx")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        log.info("Generating dynamic Excel template based on field configuration");
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        
        // Style for header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        int colIndex = 0;
        
        // CORE FIELDS - Always included in this exact order
        String[] coreFields = {
            "student_id",           // Required for identifying students
            "full_name",            // Required
            "date_of_birth",        // Standard field
            "gender",               // Standard field
            "apaar_id",
            "aadhaar_number",
            "category",
            "address",
            "photo_url",
            "previous_school_tc_url",
            "admission_class",      // Historical record (never changes)
            "current_class",        // Current class after promotions (NEW in V6)
            "admission_date",
            "enrollment_no",
            "previous_marksheet_url",
            "blood_group",
            "allergies_conditions",
            "immunization",
            "height_cm",
            "weight_kg",
            "vision_check",
            "character_cert_url",
            "aadhaar_card_url",
            "fee_status",
            "attendance_percent",
            "udise_uploaded",
            "father_name",
            "father_contact",       // Required for WhatsApp
            "father_aadhaar",
            "mother_name",
            "mother_contact",       // Required for WhatsApp
            "mother_aadhaar",
            "guardian_name",
            "guardian_contact",
            "guardian_relation",
            "guardian_aadhaar",
            "family_status",
            "language_preference"
        };
        
        // Add core field headers
        for (String fieldName : coreFields) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(fieldName);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(cell.getColumnIndex(), 20 * 256); // 20 characters wide
        }
        
        // Add dynamic fields from field_config (only active fields)
        List<FieldConfig> dynamicFields = fieldConfigRepository.findByActiveOrderBySortOrderAsc(true);
        log.info("Adding {} dynamic fields to template", dynamicFields.size());
        
        for (FieldConfig field : dynamicFields) {
            Cell cell = headerRow.createCell(colIndex++);
            String headerText = field.getDisplayName() + (field.getRequired() ? " *" : "");
            cell.setCellValue(headerText);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(cell.getColumnIndex(), 20 * 256);
            log.debug("Added dynamic field: {} ({})", field.getDisplayName(), field.getFieldName());
        }
        
        // Add example data row with hints
        Row exampleRow = sheet.createRow(1);
        CellStyle exampleStyle = workbook.createCellStyle();
        Font exampleFont = workbook.createFont();
        exampleFont.setItalic(true);
        exampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        exampleStyle.setFont(exampleFont);
        
        int exampleCol = 0;
        String[] exampleData = {
            "STU001",                   // student_id
            "John Doe",                 // full_name
            "15-01-2010",              // date_of_birth (DD-MM-YYYY)
            "Male",                     // gender
            "APAAR123456",             // apaar_id
            "123456789012",            // aadhaar_number
            "General",                  // category
            "123 Main St, City",       // address
            "https://example.com/photo.jpg", // photo_url
            "https://example.com/tc.pdf",    // previous_school_tc_url
            "5th",                      // admission_class (NEVER CHANGE)
            "6th",                      // current_class (UPDATE YEARLY)
            "15-04-2024",              // admission_date
            "ENR2024001",              // enrollment_no
            "https://example.com/marksheet.pdf", // previous_marksheet_url
            "O+",                       // blood_group
            "None",                     // allergies_conditions
            "Yes",                      // immunization
            "145",                      // height_cm
            "40",                       // weight_kg
            "Normal",                   // vision_check
            "https://example.com/character.pdf", // character_cert_url
            "https://example.com/aadhaar.pdf",   // aadhaar_card_url
            "Paid",                     // fee_status
            "95.5",                     // attendance_percent
            "Yes",                      // udise_uploaded
            "Mr. John Smith",          // father_name
            "+971508714823",           // father_contact (INTERNATIONAL FORMAT)
            "123456789012",            // father_aadhaar
            "Mrs. Jane Smith",         // mother_name
            "+919876543210",           // mother_contact (INTERNATIONAL FORMAT)
            "123456789012",            // mother_aadhaar
            "Mr. Guardian Name",       // guardian_name
            "+971501234567",           // guardian_contact
            "Uncle",                    // guardian_relation
            "123456789012",            // guardian_aadhaar
            "Both Parents",            // family_status
            "ENGLISH"                   // language_preference
        };
        
        for (int i = 0; i < exampleData.length && i < coreFields.length; i++) {
            Cell cell = exampleRow.createCell(exampleCol++);
            cell.setCellValue(exampleData[i]);
            cell.setCellStyle(exampleStyle);
        }
        
        // Add example data for dynamic fields
        for (int i = 0; i < dynamicFields.size(); i++) {
            Cell cell = exampleRow.createCell(exampleCol++);
            FieldConfig field = dynamicFields.get(i);
            String exampleValue = switch (field.getFieldType()) {
                case "STRING" -> "Sample text";
                case "NUMBER" -> "123";
                case "DATE" -> "15-01-2024";
                case "BOOLEAN" -> "Yes";
                case "FILE_URL" -> "https://example.com/file.pdf";
                default -> "Sample";
            };
            cell.setCellValue(exampleValue);
            cell.setCellStyle(exampleStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < colIndex; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] bytes = outputStream.toByteArray();
        log.info("Template generated successfully with {} columns ({} core + {} dynamic)", 
                 colIndex, coreFields.length, dynamicFields.size());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "student_upload_template.xlsx");
        headers.setContentLength(bytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}