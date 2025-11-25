package com.gradepulse.service;

import com.gradepulse.model.ClassSection;
import com.gradepulse.repository.ClassSectionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Smart service to map free-text class names from Excel uploads to structured ClassSection entities.
 * Handles various Indian school formats: CBSE, SSC, HSC, ICSE, Pre-Primary, etc.
 * 
 * Examples handled:
 * - "10-A" → 10th Grade, Section A
 * - "Class 10 A" → 10th Grade, Section A
 * - "5th Standard Section B" → 5th Grade, Section B
 * - "FYJC Science A" → Junior College, Science Stream, Section A
 * - "LKG Red" → Pre-Primary, LKG, Section Red
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassSectionMappingService {
    
    private final ClassSectionRepository classSectionRepository;
    
    /**
     * Find or create ClassSection using separate fields (preferred method).
     * 
     * @param studentClass Class name (e.g., "10", "FYJC", "LKG")
     * @param division Division/Stream (e.g., "Science", "Commerce", "General")
     * @param subDivision Section (e.g., "A", "B", "C")
     * @param academicYear Academic year (e.g., "2024-2025")
     * @param board Board (e.g., "CBSE", "SSC", "HSC")
     * @return ClassSection entity (existing or newly created)
     */
    @Transactional
    public ClassSection findOrCreateClassSection(String studentClass, String division, String subDivision, 
                                                   String academicYear, String board) {
        if (studentClass == null || studentClass.isBlank() || subDivision == null || subDivision.isBlank()) {
            log.warn("Class or sub-division is empty, returning null");
            return null;
        }
        
        // Default values
        if (board == null || board.isBlank()) board = "CBSE";
        if (division == null || division.isBlank()) division = "General";
        
        String normalizedClass = normalizeClassName(studentClass);
        String normalizedDivision = division.trim();
        String normalizedSection = subDivision.trim().toUpperCase();
        
        log.debug("Finding/creating ClassSection: class={}, division={}, section={}, year={}, board={}", 
                  normalizedClass, normalizedDivision, normalizedSection, academicYear, board);
        
        // Try to find existing ClassSection
        Optional<ClassSection> existing = classSectionRepository
            .findByAcademicYearAndBoardAndStreamAndClassNameAndSectionName(
                academicYear, board, normalizedDivision, normalizedClass, normalizedSection
            );
            
        if (existing.isPresent()) {
            log.info("Found existing ClassSection: {}", existing.get().getFullName());
            return existing.get();
        }
        
        // Create new ClassSection
        ClassSection newSection = new ClassSection();
        newSection.setAcademicYear(academicYear);
        newSection.setBoard(board);
        newSection.setStream(normalizedDivision);
        newSection.setClassName(normalizedClass);
        newSection.setSectionName(normalizedSection);
        newSection.setIsActive(true);
        
        ClassSection saved = classSectionRepository.save(newSection);
        log.info("✓ Created new ClassSection: {} (ID: {})", saved.getFullName(), saved.getId());
        
        return saved;
    }
    
    /**
     * Legacy method: Find existing ClassSection or create new one based on free-text class name.
     * This enables seamless Excel upload → Attendance marking integration.
     * 
     * @param classText Free-text class name from Excel (e.g., "10-A", "Class 5 B")
     * @param academicYear Academic year (e.g., "2024-2025")
     * @return ClassSection entity (existing or newly created)
     * @deprecated Use findOrCreateClassSection with separate fields instead
     */
    @Deprecated
    @Transactional
    public ClassSection findOrCreateClassSection(String classText, String academicYear) {
        if (classText == null || classText.isBlank()) {
            log.warn("Empty class text provided, returning null");
            return null;
        }
        
        classText = classText.trim();
        log.debug("Processing class text: '{}' for academic year: {}", classText, academicYear);
        
        // Parse the free-text into structured components
        ClassComponents components = parseClassText(classText);
        log.debug("Parsed components: board={}, stream={}, class={}, section={}", 
                  components.board, components.stream, components.className, components.sectionName);
        
        // Try to find existing ClassSection with exact match
        Optional<ClassSection> existing = classSectionRepository
            .findByAcademicYearAndBoardAndStreamAndClassNameAndSectionName(
                academicYear,
                components.board,
                components.stream,
                components.className,
                components.sectionName
            );
            
        if (existing.isPresent()) {
            log.info("Found existing ClassSection: {}", existing.get().getFullName());
            return existing.get();
        }
        
        // No match found - create new ClassSection
        ClassSection newSection = new ClassSection();
        newSection.setAcademicYear(academicYear);
        newSection.setBoard(components.board);
        newSection.setStream(components.stream);
        newSection.setClassName(components.className);
        newSection.setSectionName(components.sectionName);
        newSection.setIsActive(true);
        
        ClassSection saved = classSectionRepository.save(newSection);
        log.info("✓ Created new ClassSection: {} (ID: {})", saved.getFullName(), saved.getId());
        
        return saved;
    }
    
    /**
     * Intelligent parser for various Indian school class name formats.
     * Uses pattern matching and keyword detection to extract:
     * - Board (CBSE, SSC, HSC, ICSE, Pre-Primary)
     * - Stream (Science, Commerce, Arts, General)
     * - Class Name (1st, 2nd, ..., 12th, FYJC, SYJC, LKG, UKG)
     * - Section (A, B, C, Red, Blue, etc.)
     */
    private ClassComponents parseClassText(String text) {
        text = text.trim().toUpperCase();
        
        String board = detectBoard(text);
        String stream = detectStream(text);
        String className = detectClassName(text);
        String sectionName = detectSection(text);
        
        return new ClassComponents(board, stream, className, sectionName);
    }
    
    private String detectBoard(String text) {
        if (text.contains("CBSE")) return "CBSE";
        if (text.contains("SSC")) return "SSC";
        if (text.contains("HSC")) return "HSC";
        if (text.contains("ICSE")) return "ICSE";
        if (text.contains("LKG") || text.contains("UKG") || text.contains("NURSERY") || 
            text.contains("PRE-PRIMARY") || text.contains("PREPRIMARY")) {
            return "Pre-Primary";
        }
        
        // Default to CBSE for standard class formats
        return "CBSE";
    }
    
    private String detectStream(String text) {
        if (text.contains("SCIENCE")) return "Science";
        if (text.contains("COMMERCE")) return "Commerce";
        if (text.contains("ARTS") || text.contains("HUMANITIES")) return "Arts";
        
        // Default to General for primary/secondary classes
        return "General";
    }
    
    private String detectClassName(String text) {
        // Pre-Primary classes
        if (text.contains("LKG")) return "LKG";
        if (text.contains("UKG")) return "UKG";
        if (text.contains("NURSERY")) return "Nursery";
        
        // Junior/Senior College (Maharashtra HSC system)
        if (text.contains("FYJC") || text.contains("FY JC") || text.contains("11")) return "FYJC";
        if (text.contains("SYJC") || text.contains("SY JC") || text.contains("12")) return "SYJC";
        
        // Extract numeric class (1-12)
        Pattern numberPattern = Pattern.compile("\\b([1-9]|1[0-2])(?:ST|ND|RD|TH)?\\b");
        Matcher matcher = numberPattern.matcher(text);
        if (matcher.find()) {
            String number = matcher.group(1);
            return number + getOrdinalSuffix(Integer.parseInt(number));
        }
        
        // Fallback: Look for standalone numbers
        Pattern digitPattern = Pattern.compile("\\b([1-9]|1[0-2])\\b");
        Matcher digitMatcher = digitPattern.matcher(text);
        if (digitMatcher.find()) {
            String number = digitMatcher.group(1);
            return number + getOrdinalSuffix(Integer.parseInt(number));
        }
        
        log.warn("Could not detect class name from: {}, defaulting to 'General'", text);
        return "General";
    }
    
    private String detectSection(String text) {
        // Color-based sections (common in pre-primary)
        if (text.contains("RED")) return "Red";
        if (text.contains("BLUE")) return "Blue";
        if (text.contains("GREEN")) return "Green";
        if (text.contains("YELLOW")) return "Yellow";
        
        // Letter-based sections
        // Pattern: Space or hyphen followed by single letter A-Z
        Pattern sectionPattern = Pattern.compile("[\\s-]([A-Z])(?:\\s|$)");
        Matcher matcher = sectionPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Look for "SECTION X" format
        Pattern sectionWordPattern = Pattern.compile("SECTION\\s+([A-Z]+)");
        Matcher sectionMatcher = sectionWordPattern.matcher(text);
        if (sectionMatcher.find()) {
            return sectionMatcher.group(1);
        }
        
        // Default to A if no section detected
        return "A";
    }
    
    /**
     * Normalize class name to standard format
     */
    private String normalizeClassName(String studentClass) {
        String text = studentClass.trim().toUpperCase();
        
        // Pre-Primary classes
        if (text.contains("LKG")) return "LKG";
        if (text.contains("UKG")) return "UKG";
        if (text.contains("NURSERY")) return "Nursery";
        
        // Junior/Senior College
        if (text.contains("FYJC") || text.contains("FY") || text.equals("11")) return "FYJC";
        if (text.contains("SYJC") || text.contains("SY") || text.equals("12")) return "SYJC";
        
        // Extract numeric class (1-12)
        Pattern numberPattern = Pattern.compile("\\b([1-9]|1[0-2])\\b");
        Matcher matcher = numberPattern.matcher(text);
        if (matcher.find()) {
            String number = matcher.group(1);
            int num = Integer.parseInt(number);
            return number + getOrdinalSuffix(num);
        }
        
        // Return as-is if no pattern matched
        return studentClass.trim();
    }
    
    private String getOrdinalSuffix(int number) {
        if (number >= 11 && number <= 13) {
            return "th";
        }
        return switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
    
    /**
     * DTO to hold parsed class components
     */
    @Data
    @AllArgsConstructor
    private static class ClassComponents {
        String board;
        String stream;
        String className;
        String sectionName;
    }
}
