// src/main/java/com/gradepulse/controller/WhatsAppController.java
package com.gradepulse.controller;

import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import com.gradepulse.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppController {

    private final StudentRepository studentRepository;
    private final WhatsAppService whatsAppService;

    @PostMapping("/whatsapp")
    public ResponseEntity<String> handle(
            @RequestParam String From,
            @RequestParam String Body) {

        String from = From.replace("whatsapp:", "");
        String text = Body.trim();

        Student student = findByContact(from);
        if (student == null) {
            return ResponseEntity.ok("Student not found");
        }

        String lang = switch (text) {
            case "1" -> "ENGLISH";
            case "2" -> "HINDI";
            case "3" -> "TAMIL";
            case "4" -> "KANNADA";
            default -> {
                whatsAppService.send(from, "Please reply with 1, 2, 3, or 4.");
                yield null;
            }
        };

        if (lang != null) {
            student.setLanguagePreference(lang);
            studentRepository.save(student);
            whatsAppService.send(from, "Language set to " + lang + ". Thank you!");

            // Notify other parent
            if (student.getFatherContact() != null && !from.equals(student.getFatherContact())) {
                whatsAppService.send(student.getFatherContact(), student.getFullName() + "'s language: " + lang);
            }
            if (student.getMotherContact() != null && !from.equals(student.getMotherContact())) {
                whatsAppService.send(student.getMotherContact(), student.getFullName() + "'s language: " + lang);
            }
        }

        return ResponseEntity.ok("OK");
    }

    private Student findByContact(String contact) {
        return studentRepository.findByFatherContactOrMotherContactOrGuardianContact(
            contact, contact, contact
        );
    }
}