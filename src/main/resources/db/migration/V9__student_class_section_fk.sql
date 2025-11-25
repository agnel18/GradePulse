-- V9: Add ClassSection FK to Student table for proper attendance integration
-- This enables: Upload Students → Auto-link to ClassSection → Mark Attendance works

-- Add class_section_id foreign key column
ALTER TABLE students ADD COLUMN class_section_id BIGINT;

-- Add foreign key constraint
ALTER TABLE students ADD CONSTRAINT fk_student_class_section 
    FOREIGN KEY (class_section_id) REFERENCES class_sections(id);

-- Create index for performance (attendance queries will use this heavily)
CREATE INDEX idx_students_class_section ON students(class_section_id);

-- Keep current_class for backward compatibility
-- It will be auto-populated from classSection.fullName when FK is set
COMMENT ON COLUMN students.class_section_id IS 'FK to class_sections table. Primary method for attendance marking and class operations.';
COMMENT ON COLUMN students.current_class IS 'Legacy text field. Auto-synced with class_section.fullName for display purposes.';
