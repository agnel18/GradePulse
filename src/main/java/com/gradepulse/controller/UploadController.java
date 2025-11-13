package com.gradepulse.controller;

import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.gradepulse.service.WhatsAppService;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;



@Controller
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WhatsAppService whatsAppService;


    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        List<Student> students = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() < 2) continue; // skip header + empty

            Student s = new Student();
            s.setStudentId(getString(row, 0));           // A
            s.setFullName(getString(row, 1));            // B
            s.setDateOfBirth(getDate(row, 2));           // C
            s.setGender(getString(row, 3));              // D
            s.setApaarId(getString(row, 4));             // E
            s.setAadhaarNumber(getString(row, 5));       // F
            s.setCategory(getString(row, 6));            // G
            s.setAddress(getString(row, 7));             // H
            s.setPhoto(getBoolean(row, 8));              // I
            s.setPreviousSchoolTc(getBoolean(row, 9));   // J
            s.setAdmissionClass(getString(row, 10));     // K
            s.setAdmissionDate(getDate(row, 11));        // L
            s.setEnrollmentNo(getString(row, 12));       // M
            s.setPreviousMarksheet(getBoolean(row, 13)); // N
            s.setBloodGroup(getString(row, 14));         // O
            s.setAllergiesConditions(getString(row, 15)); // P
            s.setImmunization(getBoolean(row, 16));      // Q
            s.setHeightCm(getInt(row, 17));              // R
            s.setWeightKg(getInt(row, 18));              // S
            s.setVisionCheck(getString(row, 19));        // T
            s.setCharacterCert(getString(row, 20));      // U
            s.setFeeStatus(getString(row, 21));          // V
            s.setAttendancePercent(getDouble(row, 22));  // W
            s.setUdiseUploaded(getBoolean(row, 23));     // X

            // === FAMILY ===
            s.setFatherName(getString(row, 24));
            s.setFatherContact(getString(row, 25));
            s.setFatherAadhaar(getString(row, 26));
            s.setMotherName(getString(row, 27));
            s.setMotherContact(getString(row, 28));
            s.setMotherAadhaar(getString(row, 29));
            s.setGuardianName(getString(row, 30));
            s.setGuardianContact(getString(row, 31));
            s.setGuardianRelation(getString(row, 32));
            s.setGuardianAadhaar(getString(row, 33));
            s.setFamilyStatus(getString(row, 34));
            s.setLanguagePreference(getString(row, 35));

            // DEBUG
            log.warn("Family: Father={}, Mother={}, Lang={}", 
                s.getFatherContact(), s.getMotherContact(), s.getLanguagePreference());            
 

            // DEBUG: Show after assignment
            log.warn("After assignment:");
            log.warn("  Father Contact: {}", s.getFatherContact());
            log.warn("  Mother Contact: {}", s.getMotherContact());
            log.warn("  Guardian Contact: {}", s.getGuardianContact());

            // VALIDATE & ADD
            if (s.getStudentId() != null && 
                (s.getFatherContact() != null || s.getMotherContact() != null || s.getGuardianContact() != null)) {
                log.warn("ADDING: {} | {}", s.getFullName(), s.getFatherContact());
                students.add(s);
            } else {
                log.warn("SKIPPED: {} | ID={}", s.getFullName(), s.getStudentId() != null);
            }
        } // ← END OF FOR LOOP
    
        studentRepository.saveAll(students);

        String welcome = """
            Welcome to GradePulse!
            Reply with:
            1 → English (default)
            2 → हिंदी
            3 → தமிழ்
            4 → ಕನ್ನಡ
            """;

        for (Student s : students) {
            if (s.getFatherContact() != null) {
                whatsAppService.send(s.getFatherContact(), welcome);
            }
            if (s.getMotherContact() != null) {
                whatsAppService.send(s.getMotherContact(), welcome);
            }
        }


        workbook.close();

        model.addAttribute("message", 
            "SUCCESS! " + students.size() + " students saved → Ready for WhatsApp & UDISE+");
        model.addAttribute("totalStudents", studentRepository.count());
        return "upload";
    }

    // === HELPER METHODS ===
    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
    
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield new java.math.BigDecimal(cell.getNumericCellValue()).toPlainString();
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> null;
        };
    }

    private LocalDate getDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
    
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            try {
                return LocalDate.parse(val, DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Integer getInt(Row row, int col) {
        try {
            return (int) row.getCell(col).getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDouble(Row row, int col) {
        try {
            return row.getCell(col).getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBoolean(Row row, int col) {
        try {
            String val = getString(row, col);
            return "Yes".equalsIgnoreCase(val);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractRelation(String name) {
        if (name == null) return "Guardian";
        String lower = name.toLowerCase();
        if (lower.contains("uncle")) return "Uncle";
        if (lower.contains("aunt")) return "Aunt";
        if (lower.contains("grand")) return "Grandparent";
        if (lower.contains("guardian")) return "Legal Guardian";
        return "Guardian";
    }

    @GetMapping("/template.xlsx")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Student_Template.xlsx");

        try (Workbook workbook = new XSSFWorkbook();
            OutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Sheet1");

            // === HEADER ROW ===
            Row header = sheet.createRow(0);
            String[] headers = {
                "Student ID", "Full Name", "DOB", "Gender", "APAAR ID", "Aadhaar", "Category", "Address",
                "Photo", "TC", "Admission Class", "Admission Date", "Enrollment No", "Marksheet",
                "Blood Group", "Allergies", "Immunization", "Height", "Weight", "Vision",
                "Character Cert", "Fee Status", "Attendance %", "UDISE Uploaded",
                "Father Name", "Father Contact", "Father Aadhaar",
                "Mother Name", "Mother Contact", "Mother Aadhaar",
                "Guardian Name", "Guardian Contact", "Guardian Relation", "Guardian Aadhaar",
                "Family Status", "Language"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // === SAMPLE DATA ROW (ROW 1) ===
            Row sample = sheet.createRow(1);

            // Core Info
            sample.createCell(0).setCellValue("1001");
            sample.createCell(1).setCellValue("Priya Kumari");
            sample.createCell(2).setCellValue("15-Jan-2015");
            sample.createCell(3).setCellValue("Female");
            sample.createCell(4).setCellValue("AP987654");
            sample.createCell(5).setCellValue("123456789012");
            sample.createCell(6).setCellValue("General");
            sample.createCell(7).setCellValue("123 MG Road, Bengaluru");
            sample.createCell(8).setCellValue("Yes");           // Photo (was 9)
            sample.createCell(9).setCellValue("Yes");           // TC (was 10)
            sample.createCell(10).setCellValue("5");            // Admission Class (was 11)
            sample.createCell(11).setCellValue("01-Apr-2025");  // Admission Date (was 13)
            sample.createCell(12).setCellValue("ENR1001");      // Enrollment No (was 14)
            sample.createCell(13).setCellValue("Yes");          // Marksheet (was 15)
            sample.createCell(14).setCellValue("O+");           // Blood Group
            sample.createCell(15).setCellValue("None");         // Allergies
            sample.createCell(16).setCellValue("Yes");          // Immunization
            sample.createCell(17).setCellValue(135);            // Height
            sample.createCell(18).setCellValue(35);             // Weight
            sample.createCell(19).setCellValue("Normal");       // Vision
            sample.createCell(20).setCellValue("Good");         // Character Cert
            sample.createCell(21).setCellValue("Paid");         // Fee Status
            sample.createCell(22).setCellValue(98.5);           // Attendance %
            sample.createCell(23).setCellValue("No");           // UDISE Uploaded
            sample.createCell(24).setCellValue("Rajesh Kumar");
            sample.createCell(25).setCellValue("9987665397");
            sample.createCell(26).setCellValue("111122223333");
            sample.createCell(27).setCellValue("Sunita Devi");
            sample.createCell(28).setCellValue("8765432109");
            sample.createCell(29).setCellValue("444455556666");
            sample.createCell(30).setCellValue(""); // Guardian Name
            sample.createCell(31).setCellValue(""); // Guardian Contact
            sample.createCell(32).setCellValue(""); // Relation
            sample.createCell(33).setCellValue(""); // Aadhaar
            sample.createCell(34).setCellValue("Two Parents");
            sample.createCell(35).setCellValue("ENGLISH");
            // Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }
}