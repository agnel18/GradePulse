-- Attendance Module Tables

-- Class Sections Configuration
CREATE TABLE class_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    academic_year VARCHAR(20) NOT NULL,
    board VARCHAR(50) NOT NULL,
    stream VARCHAR(50) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    section_name VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_class_section UNIQUE (academic_year, board, stream, class_name, section_name)
);

-- Attendance Records
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    arrival_time TIME,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by VARCHAR(100),
    class_section_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    notes TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (class_section_id) REFERENCES class_sections(id) ON DELETE CASCADE,
    CONSTRAINT unique_attendance UNIQUE (student_id, attendance_date)
);

-- Indexes for performance
CREATE INDEX idx_attendance_date ON attendance_records(attendance_date);
CREATE INDEX idx_attendance_student ON attendance_records(student_id);
CREATE INDEX idx_attendance_class_section ON attendance_records(class_section_id);
CREATE INDEX idx_class_sections_active ON class_sections(is_active, academic_year);

-- Insert default class sections for common Indian education system
-- Pre-Primary
INSERT INTO class_sections (academic_year, board, stream, class_name, section_name) VALUES
('2024-2025', 'Pre-Primary', 'General', 'Playgroup', 'A'),
('2024-2025', 'Pre-Primary', 'General', 'Nursery', 'A'),
('2024-2025', 'Pre-Primary', 'General', 'LKG', 'A'),
('2024-2025', 'Pre-Primary', 'General', 'UKG', 'A');

-- Primary & Secondary (CBSE)
INSERT INTO class_sections (academic_year, board, stream, class_name, section_name) VALUES
('2024-2025', 'CBSE', 'General', '1st', 'A'),
('2024-2025', 'CBSE', 'General', '2nd', 'A'),
('2024-2025', 'CBSE', 'General', '3rd', 'A'),
('2024-2025', 'CBSE', 'General', '4th', 'A'),
('2024-2025', 'CBSE', 'General', '5th', 'A'),
('2024-2025', 'CBSE', 'General', '6th', 'A'),
('2024-2025', 'CBSE', 'General', '7th', 'A'),
('2024-2025', 'CBSE', 'General', '8th', 'A'),
('2024-2025', 'CBSE', 'General', '9th', 'A'),
('2024-2025', 'CBSE', 'General', '10th', 'A'),
('2024-2025', 'CBSE', 'Science', '11th', 'A'),
('2024-2025', 'CBSE', 'Commerce', '11th', 'B'),
('2024-2025', 'CBSE', 'Science', '12th', 'A'),
('2024-2025', 'CBSE', 'Commerce', '12th', 'B');

-- SSC/State Board
INSERT INTO class_sections (academic_year, board, stream, class_name, section_name) VALUES
('2024-2025', 'SSC', 'General', '5th', 'A'),
('2024-2025', 'SSC', 'General', '10th', 'A');

-- College (Junior College & Degree)
INSERT INTO class_sections (academic_year, board, stream, class_name, section_name) VALUES
('2024-2025', 'HSC', 'Science', 'FYJC', 'A'),
('2024-2025', 'HSC', 'Commerce', 'FYJC', 'B'),
('2024-2025', 'HSC', 'Science', 'SYJC', 'A'),
('2024-2025', 'HSC', 'Commerce', 'SYJC', 'B'),
('2024-2025', 'University', 'Science', 'FY BSc', 'A'),
('2024-2025', 'University', 'Commerce', 'FY BCom', 'A'),
('2024-2025', 'University', 'Science', 'SY BSc', 'A'),
('2024-2025', 'University', 'Science', 'TY BSc', 'A');
