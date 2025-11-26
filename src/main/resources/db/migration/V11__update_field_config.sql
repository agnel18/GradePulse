-- V11: Update field configuration - Add essential fields, disable health tracking by default
-- Date: 2025-11-26

-- Add essential fields that were missing
INSERT INTO field_config (field_name, display_name, field_type, required, active, sort_order)
VALUES
('student_class', 'Class', 'STRING', TRUE, TRUE, 37),
('division', 'Division/Stream', 'STRING', FALSE, TRUE, 38),
('sub_division', 'Sub-Division/Section', 'STRING', TRUE, TRUE, 39);

-- Disable health tracking fields by default (schools rarely track these annually)
UPDATE field_config SET active = FALSE WHERE field_name IN (
    'allergies_conditions',
    'immunization', 
    'height_cm',
    'weight_kg',
    'vision_check'
);

-- Make APAAR ID active but not overwhelming (it's new and not all schools have it yet)
UPDATE field_config SET active = TRUE WHERE field_name = 'apaar_id';

COMMENT ON TABLE field_config IS 'V11: Added Class/Division/Sub-Division fields, disabled health tracking fields by default';
