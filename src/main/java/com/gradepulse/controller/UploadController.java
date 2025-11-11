package com.gradepulse.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class UploadController {

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        AtomicInteger count = new AtomicInteger(0);

        sheet.rowIterator().forEachRemaining(row -> {
            if (row.getRowNum() == 0) return;
            String name = getCellValue(row.getCell(0));
            String phone = getCellValue(row.getCell(1));
            String marks = getCellValue(row.getCell(2));
            if (name != null && phone != null) count.incrementAndGet();
        });

        workbook.close();
        model.addAttribute("message", 
            "SUCCESS! " + count.get() + " students processed → WhatsApp ready!");
        return "upload";
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }

    @GetMapping("/template.xlsx")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=GradePulse_Template.xlsx");
        
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Grades");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student Name");
        header.createCell(1).setCellValue("Parent Phone (+9198...)");
        header.createCell(2).setCellValue("Marks (out of 20)");
        wb.write(response.getOutputStream());
        wb.close();
    }
}