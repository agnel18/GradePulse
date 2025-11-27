package com.gradepulse.controller;

import com.gradepulse.model.Student;
import com.gradepulse.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            Model model) {
        
        // Normalize empty strings to null (create new final variables for lambda use)
        final String finalSearch = (search != null && search.trim().isEmpty()) ? null : search;
        final String finalSchoolName = (schoolName != null && schoolName.trim().isEmpty()) ? null : schoolName;
        final String finalBoard = (board != null && board.trim().isEmpty()) ? null : board;
        final String finalAcademicYear = (academicYear != null && academicYear.trim().isEmpty()) ? null : academicYear;
        final String finalStudentClass = (studentClass != null && studentClass.trim().isEmpty()) ? null : studentClass;
        final String finalDivision = (division != null && division.trim().isEmpty()) ? null : division;
        final String finalGender = (gender != null && gender.trim().isEmpty()) ? null : gender;
        
        List<Student> students = studentRepository.findAll();
        
        // Apply filters
        if (finalSearch != null && !finalSearch.trim().isEmpty()) {
            String s = finalSearch.toLowerCase();
            students = students.stream()
                .filter(st -> (st.getStudentId() != null && st.getStudentId().toLowerCase().contains(s)) ||
                             (st.getFullName() != null && st.getFullName().toLowerCase().contains(s)) ||
                             (st.getFatherContact() != null && st.getFatherContact().contains(s)) ||
                             (st.getMotherContact() != null && st.getMotherContact().contains(s)))
                .toList();
        }
        
        if (finalSchoolName != null && !finalSchoolName.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getSchoolName() != null && st.getSchoolName().equals(finalSchoolName))
                .toList();
        }
        
        if (finalBoard != null && !finalBoard.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getBoard() != null && st.getBoard().equals(finalBoard))
                .toList();
        }
        
        if (finalAcademicYear != null && !finalAcademicYear.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getAcademicYear() != null && st.getAcademicYear().equals(finalAcademicYear))
                .toList();
        }
        
        if (finalStudentClass != null && !finalStudentClass.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getStudentClass() != null && st.getStudentClass().equals(finalStudentClass))
                .toList();
        }
        
        if (finalDivision != null && !finalDivision.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getDivision() != null && st.getDivision().equals(finalDivision))
                .toList();
        }
        
        if (finalGender != null && !finalGender.trim().isEmpty()) {
            students = students.stream()
                .filter(st -> st.getGender() != null && st.getGender().equalsIgnoreCase(finalGender))
                .toList();
        }
        
        // Apply pagination to filtered list
        int filteredSize = students != null ? students.size() : 0;
        int totalPages = filteredSize > 0 ? (int) Math.ceil((double) filteredSize / size) : 0;
        
        // Ensure page is within bounds
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        
        int start = Math.min(page * size, filteredSize);
        int end = Math.min(start + size, filteredSize);
        List<Student> paginatedStudents = filteredSize > 0 ? students.subList(start, end) : new ArrayList<>();
        
        // Get unique values for filter dropdowns
        List<Student> allStudents = studentRepository.findAll();
        model.addAttribute("schools", allStudents.stream().map(Student::getSchoolName).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("boards", allStudents.stream().map(Student::getBoard).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("academicYears", allStudents.stream().map(Student::getAcademicYear).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("classes", allStudents.stream().map(Student::getStudentClass).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("divisions", allStudents.stream().map(Student::getDivision).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        
        model.addAttribute("students", paginatedStudents);
        model.addAttribute("totalStudents", allStudents.size());
        model.addAttribute("filteredCount", filteredSize);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        log.info("Listing {} students on page {} of {} (filtered from {} total)", paginatedStudents.size(), page + 1, Math.max(totalPages, 1), allStudents.size());
        return "students-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("pageTitle", "Add New Student");
        model.addAttribute("formAction", "/students/save");
        // Dropdown options from existing data
        List<Student> allStudents = studentRepository.findAll();
        model.addAttribute("boards", allStudents.stream().map(Student::getBoard).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("academicYears", allStudents.stream().map(Student::getAcademicYear).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("classes", allStudents.stream().map(Student::getStudentClass).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("divisions", allStudents.stream().map(Student::getDivision).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
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
        // Dropdown options from existing data
        List<Student> allStudents = studentRepository.findAll();
        model.addAttribute("boards", allStudents.stream().map(Student::getBoard).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("academicYears", allStudents.stream().map(Student::getAcademicYear).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("classes", allStudents.stream().map(Student::getStudentClass).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
        model.addAttribute("divisions", allStudents.stream().map(Student::getDivision).filter(s -> s != null && !s.isEmpty()).distinct().sorted().toList());
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
