package com.gradepulse.controller;

import com.gradepulse.model.FieldConfig;
import com.gradepulse.repository.FieldConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FieldConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(FieldConfigController.class);
    
    @Autowired
    private FieldConfigRepository fieldConfigRepository;

    @GetMapping("/fields")
    public String listFields(Model model) {
        List<FieldConfig> fields = fieldConfigRepository.findAllByOrderBySortOrderAsc();
        model.addAttribute("fields", fields);
        model.addAttribute("totalFields", fields.size());
        model.addAttribute("activeFields", fields.stream().filter(FieldConfig::getActive).count());
        log.info("Loaded {} fields", fields.size());
        return "fields";
    }

    @PostMapping("/fields/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addField(@RequestBody FieldConfig field) {
        log.info("Adding new field: {}", field.getFieldName());
        
        // Validate
        if (fieldConfigRepository.existsByFieldName(field.getFieldName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Field name already exists"));
        }
        
        // Set default sort order (last)
        Integer maxOrder = fieldConfigRepository.findMaxSortOrder();
        field.setSortOrder(maxOrder == null ? 0 : maxOrder + 1);
        
        FieldConfig saved = fieldConfigRepository.save(field);
        log.info("Field saved with ID: {}", saved.getId());
        
        return ResponseEntity.ok(Map.of("success", true, "field", saved));
    }

    @PostMapping("/fields/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateField(@PathVariable Long id, @RequestBody FieldConfig updatedField) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        
        log.info("Updating field ID: {}", id);
        
        return fieldConfigRepository.findById(id).map(field -> {
            if (field == null) {
                return ResponseEntity.notFound().<Map<String, Object>>build();
            }
            field.setDisplayName(updatedField.getDisplayName());
            field.setFieldType(updatedField.getFieldType());
            field.setRequired(updatedField.getRequired());
            field.setActive(updatedField.getActive());
            
            FieldConfig saved = fieldConfigRepository.save(field);
            log.info("Field updated: {}", saved.getFieldName());
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "field", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleField(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        return fieldConfigRepository.findById(id).map(field -> {
            field.setActive(!field.getActive());
            fieldConfigRepository.save(field);
            log.info("Field {} toggled to {}", field.getFieldName(), field.getActive() ? "active" : "inactive");
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "active", field.getActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteField(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID cannot be null"));
        }
        
        return fieldConfigRepository.findById(id).map(field -> {
            if (field == null) {
                return ResponseEntity.notFound().<Map<String, Object>>build();
            }
            fieldConfigRepository.delete(field);
            log.info("Field deleted: {}", field.getFieldName());
            
            return ResponseEntity.ok(Map.<String, Object>of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fields/reorder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reorderFields(@RequestBody List<Long> fieldIds) {
        log.info("Reordering {} fields", fieldIds.size());
        
        for (int i = 0; i < fieldIds.size(); i++) {
            final int sortOrder = i;
            Long fieldId = fieldIds.get(i);
            if (fieldId != null) {
                fieldConfigRepository.findById(fieldId).ifPresent(field -> {
                    field.setSortOrder(sortOrder);
                    fieldConfigRepository.save(field);
                });
            }
        }
        
        return ResponseEntity.ok(Map.of("success", true));
    }
}