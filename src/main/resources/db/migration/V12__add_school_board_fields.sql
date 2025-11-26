-- V12: Add School Name and Board fields
-- Date: 2025-11-26
-- Purpose: Support multi-school management groups and explicit board tracking

-- Add school_name field (for management groups with multiple schools)
ALTER TABLE students ADD COLUMN school_name VARCHAR(255);
COMMENT ON COLUMN students.school_name IS 'Name of the school (e.g., Cambridge High School, Ryan International) - for groups managing multiple schools';

-- Add board field (explicit board affiliation)
ALTER TABLE students ADD COLUMN board VARCHAR(100);
COMMENT ON COLUMN students.board IS 'Educational board (e.g., CBSE, ICSE, SSC, HSC, Mumbai University, Cambridge)';

-- Add academic_year field (student''s current academic year)
ALTER TABLE students ADD COLUMN academic_year VARCHAR(20);
COMMENT ON COLUMN students.academic_year IS 'Academic year (e.g., 2024-2025) for the student';

-- Add index for common queries
CREATE INDEX idx_students_school_board ON students(school_name, board);
CREATE INDEX idx_students_academic_year ON students(academic_year);

COMMENT ON TABLE students IS 'V12: Added school_name, board, academic_year for multi-school management support';
