package com.gradepulse.repository;

import com.gradepulse.model.FieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FieldConfigRepository extends JpaRepository<FieldConfig, Long> {
    List<FieldConfig> findByActiveTrueOrderBySortOrderAsc();
    
    List<FieldConfig> findAllByOrderBySortOrderAsc();
    
    boolean existsByFieldName(String fieldName);
    
    @Query("SELECT MAX(f.sortOrder) FROM FieldConfig f")
    Integer findMaxSortOrder();
}