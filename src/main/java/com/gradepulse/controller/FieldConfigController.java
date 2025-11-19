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
        
        // Fix display names if they haven't been updated yet
        fields.stream()
            .filter(f -> "previous_school_tc_url".equals(f.getFieldName()) && "TC".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Transfer Certificate (TC)");
                fieldConfigRepository.save(f);
                log.info("Updated TC field display name");
            });
        
        fields.stream()
            .filter(f -> "aadhaar_number".equals(f.getFieldName()) && "Aadhaar".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Student Aadhaar");
                fieldConfigRepository.save(f);
                log.info("Updated Aadhaar field display name");
            });
        
        fields.stream()
            .filter(f -> "character_cert_url".equals(f.getFieldName()) && "Character Cert".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Character Certificate");
                fieldConfigRepository.save(f);
                log.info("Updated Character Certificate field display name");
            });
        
        fields.stream()
            .filter(f -> "apaar_id".equals(f.getFieldName()) && "APAAR ID".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("APAAR ID (Automated Permanent Academic Account Registry)");
                fieldConfigRepository.save(f);
                log.info("Updated APAAR ID field display name");
            });
        
        fields.stream()
            .filter(f -> "udise_uploaded".equals(f.getFieldName()) && "UDISE Uploaded".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("UDISE Uploaded (Unified District Information System for Education)");
                fieldConfigRepository.save(f);
                log.info("Updated UDISE field display name");
            });
        
        fields.stream()
            .filter(f -> "height_cm".equals(f.getFieldName()) && "Height".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Height (cm)");
                fieldConfigRepository.save(f);
                log.info("Updated Height field display name");
            });
        
        fields.stream()
            .filter(f -> "weight_kg".equals(f.getFieldName()) && "Weight".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Weight (kg)");
                fieldConfigRepository.save(f);
                log.info("Updated Weight field display name");
            });
        
        fields.stream()
            .filter(f -> "vision_check".equals(f.getFieldName()) && "Vision".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Vision (6/6 or Diopters)");
                fieldConfigRepository.save(f);
                log.info("Updated Vision field display name");
            });
        
        fields.stream()
            .filter(f -> "date_of_birth".equals(f.getFieldName()) && "DOB".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("DOB (DD/MM/YYYY)");
                fieldConfigRepository.save(f);
                log.info("Updated DOB field display name");
            });
        
        fields.stream()
            .filter(f -> "admission_date".equals(f.getFieldName()) && "Admission Date".equals(f.getDisplayName()))
            .findFirst()
            .ifPresent(f -> {
                f.setDisplayName("Admission Date (DD/MM/YYYY)");
                fieldConfigRepository.save(f);
                log.info("Updated Admission Date field display name");
            });
        
        model.addAttribute("fields", fields);
        model.addAttribute("totalFields", fields.size());
        model.addAttribute("activeFields", fields.stream().filter(FieldConfig::getActive).count());
        // Custom fields are those beyond the 36 core fields loaded from migration
        model.addAttribute("customFields", Math.max(0, fields.size() - 36));
        log.info("Loaded {} fields ({} custom)", fields.size(), Math.max(0, fields.size() - 36));
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
     * Only includes fields where active=true, in sort_order sequence
     */
    @GetMapping("/template.xlsx")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        log.info("Generating dynamic Excel template based on active field configuration");
        
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
        
        // Get all active fields in sort order from database
        List<FieldConfig> activeFields = fieldConfigRepository.findByActiveOrderBySortOrderAsc(true);
        log.info("Generating template with {} active fields", activeFields.size());
        
        // Create TEXT format style for all data cells
        DataFormat format = workbook.createDataFormat();
        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(format.getFormat("@")); // @ = TEXT format
        
        // Create TEXT style with example formatting
        CellStyle exampleTextStyle = workbook.createCellStyle();
        exampleTextStyle.setDataFormat(format.getFormat("@"));
        Font exampleFont = workbook.createFont();
        exampleFont.setItalic(true);
        exampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        exampleTextStyle.setFont(exampleFont);
        
        int colIndex = 0;
        
        // Add all active field headers with display names
        for (FieldConfig field : activeFields) {
            Cell cell = headerRow.createCell(colIndex);
            String headerText = field.getDisplayName() + (field.getRequired() ? " *" : "");
            cell.setCellValue(headerText);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(colIndex, 25 * 256);
            colIndex++;
        }
        
        // Pre-format 100 rows as TEXT to ensure Excel recognizes the format
        for (int rowNum = 1; rowNum <= 100; rowNum++) {
            Row row = sheet.createRow(rowNum);
            for (int col = 0; col < colIndex; col++) {
                Cell cell = row.createCell(col);
                cell.setCellStyle(rowNum == 1 ? exampleTextStyle : textStyle);
            }
        }
        
        // Add example data in row 1
        Row exampleRow = sheet.getRow(1);
        int exampleCol = 0;
        
        // Add example data for all active fields based on their type
        for (FieldConfig field : activeFields) {
            Cell cell = exampleRow.getCell(exampleCol++);
            String exampleValue = switch (field.getFieldType()) {
                case "STRING" -> "Sample text";
                case "NUMBER" -> "123";
                case "DATE" -> "15-01-2024";
                case "BOOLEAN" -> "Yes";
                case "FILE_URL" -> "https://example.com/file.pdf";
                default -> "Sample";
            };
            cell.setCellValue(exampleValue);
            // Style already applied when cell was created
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
        log.info("Template generated successfully with {} active columns", colIndex);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "student_upload_template.xlsx");
        headers.setContentLength(bytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}