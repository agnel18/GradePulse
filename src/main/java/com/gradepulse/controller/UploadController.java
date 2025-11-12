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
import java.math.BigDecimal;

@Controller
public class UploadController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WhatsAppService whatsAppService;


    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

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
            s.setPhoto(getBoolean(row, 9));           // J → Photo
            s.setPreviousSchoolTc(getBoolean(row, 10)); // K → TC
            s.setAdmissionClass(getString(row, 11));   // L → Admission Class
            s.setAdmissionDate(getDate(row, 13));      // N → Admission Date
            s.setEnrollmentNo(getString(row, 14));     // O → Enrollment No
            s.setPreviousMarksheet(getBoolean(row, 15)); // P → Marksheet
            s.setBloodGroup(getString(row, 16));       // Q → Blood Group
            s.setAllergiesConditions(getString(row, 17)); // R → Allergies
            s.setImmunization(getBoolean(row, 18));    // S → Immunization
            s.setHeightCm(getInt(row, 19));            // T → Height
            s.setWeightKg(getInt(row, 20));            // U → Weight
            s.setVisionCheck(getString(row, 21));      // V → Vision
            s.setCharacterCert(getString(row, 22));    // W → Character Cert
            s.setFeeStatus(getString(row, 23));        // X → Fee Status
            s.setAttendancePercent(getDouble(row, 24)); // Y → Attendance %
            s.setUdiseUploaded(getBoolean(row, 25));   // Z → UDISE Uploaded

            // === INCLUSIVE FAMILY PARSING ===
            String rawFamily = getString(row, 8);      // I → Parent Name
            String rawContact = getString(row, 12);    // M → Parent Contact (CORRECT!)

            if (rawFamily != null) {
                rawFamily = rawFamily.trim();
                if (rawFamily.contains(" & ")) {
                    String[] parts = rawFamily.split(" & ", 2);
                    s.setFatherName(parts[0].trim());
                    s.setMotherName(parts.length > 1 ? parts[1].trim() : null);
                    s.setFamilyStatus("Two Parents");
                } else if (rawFamily.toLowerCase().contains("father")) {
                    s.setFatherName(rawFamily.replaceAll("(?i)father of.*", "").trim());
                    s.setFamilyStatus("Single Father");
                } else if (rawFamily.toLowerCase().contains("mother")) {
                    s.setMotherName(rawFamily.replaceAll("(?i)mother of.*", "").trim());
                    s.setFamilyStatus("Single Mother");
                } else {
                    s.setGuardianName(rawFamily);
                    s.setGuardianRelation(extractRelation(rawFamily));
                    s.setFamilyStatus("Guardian");
                }
            }

            // Assign primary contact
            if (rawContact != null) {
                if (s.getFatherName() != null && s.getFatherContact() == null) {
                    s.setFatherContact(rawContact);
                } else if (s.getMotherName() != null && s.getMotherContact() == null) {
                    s.setMotherContact(rawContact);
                } else if (s.getGuardianName() != null) {
                    s.setGuardianContact(rawContact);
                }
            }

            // VALIDATE: Must have student ID and at least one contact
            // TEST MODE: Allow all valid rows
            if (s.getStudentId() != null && 
            (s.getFatherContact() != null || s.getMotherContact() != null || s.getGuardianContact() != null)) {
            System.out.println("Adding: " + s.getFullName() + " | Contact: " + s.getFatherContact());
            students.add(s);
            }
        }

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
    
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            java.util.Date utilDate = cell.getDateCellValue();
            return utilDate.toInstant()
                          .atZone(java.time.ZoneId.systemDefault())
                          .toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim(), dateFormatter);
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
        // Same as before
    }
}