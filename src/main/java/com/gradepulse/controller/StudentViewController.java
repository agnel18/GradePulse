package com.gradepulse.controller;

import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentViewController {

    private static final Logger log = LoggerFactory.getLogger(StudentViewController.class);

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public String listStudents(Model model) {
        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);
        model.addAttribute("totalStudents", students.size());
        log.info("Listing {} students", students.size());
        return "students-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("pageTitle", "Add New Student");
        model.addAttribute("formAction", "/students/save");
        return "student-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            redirectAttributes.addFlashAttribute("error", "Student not found with ID: " + id);
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "Edit Student: " + student.getFullName());
        model.addAttribute("formAction", "/students/update/" + id);
        return "student-form";
    }

    @PostMapping("/save")
    public String saveStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student added successfully: " + student.getFullName());
            log.info("Saved new student: {} (ID: {})", student.getFullName(), student.getStudentId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save student: " + e.getMessage());
            log.error("Error saving student", e);
        }
        return "redirect:/students";
    }

    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            Student existing = studentRepository.findById(id).orElse(null);
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Student not found");
                return "redirect:/students";
            }
            student.setId(id);
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student updated successfully: " + student.getFullName());
            log.info("Updated student: {} (ID: {})", student.getFullName(), student.getStudentId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update student: " + e.getMessage());
            log.error("Error updating student", e);
        }
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentRepository.findById(id).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "Student not found");
                return "redirect:/students";
            }
            String name = student.getFullName();
            studentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted: " + name);
            log.info("Deleted student: {}", name);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete student: " + e.getMessage());
            log.error("Error deleting student", e);
        }
        return "redirect:/students";
    }

    @GetMapping("/view/{id}")
    public String viewStudent(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            redirectAttributes.addFlashAttribute("error", "Student not found");
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        log.info("Viewing student details: {}", student.getFullName());
        return "student-detail";
    }
}
