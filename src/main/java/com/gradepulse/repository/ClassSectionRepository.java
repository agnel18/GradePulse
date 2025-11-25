package com.gradepulse.repository;

import com.gradepulse.model.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    List<ClassSection> findByIsActiveTrue();

    List<ClassSection> findByAcademicYearAndIsActiveTrue(String academicYear);

    @Query("SELECT DISTINCT cs.academicYear FROM ClassSection cs WHERE cs.isActive = true ORDER BY cs.academicYear DESC")
    List<String> findDistinctAcademicYears();

    @Query("SELECT DISTINCT cs.board FROM ClassSection cs WHERE cs.academicYear = ?1 AND cs.isActive = true ORDER BY cs.board")
    List<String> findDistinctBoardsByAcademicYear(String academicYear);

    @Query("SELECT DISTINCT cs.stream FROM ClassSection cs WHERE cs.academicYear = ?1 AND cs.board = ?2 AND cs.isActive = true ORDER BY cs.stream")
    List<String> findDistinctStreamsByAcademicYearAndBoard(String academicYear, String board);

    @Query("SELECT DISTINCT cs.className FROM ClassSection cs WHERE cs.academicYear = ?1 AND cs.board = ?2 AND cs.stream = ?3 AND cs.isActive = true ORDER BY cs.className")
    List<String> findDistinctClassNamesByAcademicYearBoardAndStream(String academicYear, String board, String stream);

    List<ClassSection> findByAcademicYearAndBoardAndStreamAndClassNameAndIsActiveTrue(
        String academicYear, String board, String stream, String className);

    Optional<ClassSection> findByAcademicYearAndBoardAndStreamAndClassNameAndSectionName(
        String academicYear, String board, String stream, String className, String sectionName);
    
    // NEW: Search by fullName format (for legacy string matching)
    @Query("SELECT cs FROM ClassSection cs WHERE cs.academicYear = :academicYear " +
           "AND LOWER(CONCAT(cs.board, ' - ', cs.stream, ' ', cs.className, ' (Section ', cs.sectionName, ')')) " +
           "LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ClassSection> findByFullNameContainingIgnoreCaseAndAcademicYear(String searchText, String academicYear);
}
