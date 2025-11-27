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
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentViewController {

    private static final Logger log = LoggerFactory.getLogger(StudentViewController.class);

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public String listStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String board,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String studentClass,
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String gender,
            Model model) {
        
        List<Student> students = studentRepository.findAll();
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase();
            students = students.stream()
                .filter(st -> (st.getStudentId() != null && st.getStudentId().toLowerCase().contains(s)) ||
                             (st.getFullName() != null && st.getFullName().toLowerCase().contains(s)) ||
                             (st.getFatherContact() != null && st.getFatherContact().contains(s)) ||
                             (st.getMotherContact() != null && st.getMotherContact().contains(s)))
                .toList();
        }
        
        if (schoolName != null && !schoolName.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getSchoolName() != null && st.getSchoolName().equals(schoolName))
                .toList();
        }
        
        if (board != null && !board.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getBoard() != null && st.getBoard().equals(board))
                .toList();
        }
        
        if (academicYear != null && !academicYear.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getAcademicYear() != null && st.getAcademicYear().equals(academicYear))
                .toList();
        }
        
        if (studentClass != null && !studentClass.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getStudentClass() != null && st.getStudentClass().equals(studentClass))
                .toList();
        }
        
        if (division != null && !division.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getDivision() != null && st.getDivision().equals(division))
                .toList();
        }
        
        if (gender != null && !gender.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getGender() != null && st.getGender().equalsIgnoreCase(gender))
                .toList();
        }
        
        // Get unique values for filter dropdowns
        List<Student> allStudents = studentRepository.findAll();
        model.addAttribute("schools", allStudents.stream().map(Student::getSchoolName).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("boards", allStudents.stream().map(Student::getBoard).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("academicYears", allStudents.stream().map(Student::getAcademicYear).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("classes", allStudents.stream().map(Student::getStudentClass).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("divisions", allStudents.stream().map(Student::getDivision).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        
        model.addAttribute("students", students);
        model.addAttribute("totalStudents", allStudents.size());
        model.addAttribute("filteredCount", students.size());
        log.info("Listing {} students (filtered from {})", students.size(), allStudents.size());
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
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
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
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Student");
            model.addAttribute("formAction", "/students/save");
            return "student-form";
        }
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
    public String updateStudent(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Student existing = studentRepository.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "Student not found");
            return "redirect:/students";
        }
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Student: " + (student.getFullName() != null ? student.getFullName() : ""));
            model.addAttribute("formAction", "/students/update/" + id);
            return "student-form";
        }
        try {
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
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
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
    public String viewStudent(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
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
