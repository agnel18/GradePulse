-- V6: Add current_class field to students table for tracking yearly promotions
-- admission_class = class when student first joined (never changes)
-- current_class = student's present class (updated yearly during promotion)

ALTER TABLE students ADD COLUMN current_class VARCHAR(255);

-- For existing students, copy admission_class to current_class as initial value
UPDATE students SET current_class = admission_class WHERE current_class IS NULL;

-- Add comment for clarity
COMMENT ON COLUMN students.current_class IS 'Current class of student (updated yearly). Use this for attendance marking and academic operations.';
COMMENT ON COLUMN students.admission_class IS 'Class at time of admission (historical record, never changes).';
