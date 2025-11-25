package com.gradepulse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_config")
@Data @NoArgsConstructor @AllArgsConstructor
public class FieldConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fieldName;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String fieldType;          // STRING, NUMBER, DATE, BOOLEAN, FILE_URL

    @Column(length = 500)
    private String description;         // Custom description for the field

    private Boolean required = false;
    private Boolean active = true;
    private Integer sortOrder = 0;
}