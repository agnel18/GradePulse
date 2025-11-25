-- Migration V10: Add separate class, division, and sub-division fields
-- Date: 2025-11-25
-- Purpose: Add granular class fields for better organization and easier data entry

-- Add new columns for granular class information
ALTER TABLE students ADD COLUMN student_class VARCHAR(50);
COMMENT ON COLUMN students.student_class IS 'Class/Standard (e.g., 10, FYJC, LKG)';

ALTER TABLE students ADD COLUMN division VARCHAR(50);
COMMENT ON COLUMN students.division IS 'Division/Stream (e.g., Science, Commerce, Arts, General)';

ALTER TABLE students ADD COLUMN sub_division VARCHAR(10);
COMMENT ON COLUMN students.sub_division IS 'Sub-Division/Section (e.g., A, B, C, Red, Blue)';

-- Add index for faster queries by class+division+subdivision
CREATE INDEX idx_students_class_division ON students(student_class, division, sub_division);

COMMENT ON TABLE students IS 'Updated to include granular class fields (V10): student_class, division, sub_division for seamless upload integration';
