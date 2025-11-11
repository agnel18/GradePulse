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

@Controller
public class UploadController {

    @Autowired
    private StudentRepository studentRepository;

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
            s.setStudentId(getString(row, 0));
            s.setFullName(getString(row, 1));
            s.setDateOfBirth(getDate(row, 2));
            s.setGender(getString(row, 3));
            s.setApaarId(getString(row, 4));
            s.setAadhaarNumber(getString(row, 5));
            s.setCategory(getString(row, 6));
            s.setAddress(getString(row, 7));
            s.setPhoto(getBoolean(row, 13));
            s.setPreviousSchoolTc(getBoolean(row, 14));
            s.setAdmissionClass(getString(row, 15));
            s.setAdmissionDate(getDate(row, 16));
            s.setEnrollmentNo(getString(row, 17));
            s.setPreviousMarksheet(getBoolean(row, 18));
            s.setBloodGroup(getString(row, 19));
            s.setAllergiesConditions(getString(row, 20));
            s.setImmunization(getBoolean(row, 21));
            s.setHeightCm(getInt(row, 22));
            s.setWeightKg(getInt(row, 23));
            s.setVisionCheck(getString(row, 24));
            s.setCharacterCert(getString(row, 25));
            s.setFeeStatus(getString(row, 26));
            s.setAttendancePercent(getDouble(row, 27));
            s.setUdiseUploaded(getBoolean(row, 28));

            // === INCLUSIVE FAMILY PARSING (INSIDE LOOP) ===
            String rawFamily = getString(row, 8);  // e.g., "Rajesh & Sunita"
            String rawContact = getString(row, 11);

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
            if (s.getStudentId() != null && 
                (s.getFatherContact() != null || s.getMotherContact() != null || s.getGuardianContact() != null) &&
                !studentRepository.existsByFatherContactOrMotherContactOrGuardianContact(
                    s.getFatherContact(), s.getMotherContact(), s.getGuardianContact())) {
                students.add(s);
            }
        }

        studentRepository.saveAll(students);
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
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private LocalDate getDate(Row row, int col) {
        try {
            Cell cell = row.getCell(col);
            if (cell == null) return null;
            String val = cell.getStringCellValue().trim();
            return LocalDate.parse(val, dateFormatter);
        } catch (Exception e) {
            return null;
        }
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