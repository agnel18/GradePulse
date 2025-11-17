-- 1. Dynamic field configuration
CREATE TABLE field_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL,   -- STRING, NUMBER, DATE, BOOLEAN
    required BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add JSON column to students (stores custom fields)
ALTER TABLE students
    ADD COLUMN dynamic_data JSON;

-- 3. Insert default core fields (user can edit/delete)
INSERT INTO field_config (field_name, display_name, field_type, required, sort_order) VALUES
('student_id',          'Student ID',          'STRING',  TRUE,  10),
('full_name',           'Full Name',           'STRING',  TRUE,  20),
('father_contact',      'Father Contact',      'STRING',  TRUE,  30),
('mother_contact',      'Mother Contact',      'STRING',  FALSE, 40),
('language_preference', 'Language Preference', 'STRING',  FALSE, 50);