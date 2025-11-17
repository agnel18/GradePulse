-- V4__default_field_config.sql
-- Pre-populate field_config table with all default student fields for GradePulse
-- Delete existing records first to avoid duplicates
DELETE FROM field_config;

INSERT INTO field_config (field_name, display_name, field_type, required, active, sort_order)
VALUES
('student_id', 'Student ID', 'STRING', TRUE, TRUE, 1),
('full_name', 'Full Name', 'STRING', TRUE, TRUE, 2),
('date_of_birth', 'DOB', 'DATE', FALSE, TRUE, 3),
('gender', 'Gender', 'STRING', FALSE, TRUE, 4),
('apaar_id', 'APAAR ID', 'STRING', FALSE, TRUE, 5),
('aadhaar_number', 'Aadhaar', 'STRING', FALSE, TRUE, 6),
('category', 'Category', 'STRING', FALSE, TRUE, 7),
('address', 'Address', 'STRING', FALSE, TRUE, 8),
('photo_url', 'Photo', 'FILE_URL', FALSE, TRUE, 9),
('previous_school_tc_url', 'TC', 'FILE_URL', FALSE, TRUE, 10),
('admission_class', 'Admission Class', 'STRING', FALSE, TRUE, 11),
('admission_date', 'Admission Date', 'DATE', FALSE, TRUE, 12),
('enrollment_no', 'Enrollment No', 'STRING', FALSE, TRUE, 13),
('previous_marksheet_url', 'Marksheet', 'FILE_URL', FALSE, TRUE, 14),
('blood_group', 'Blood Group', 'STRING', FALSE, TRUE, 15),
('allergies_conditions', 'Allergies', 'STRING', FALSE, TRUE, 16),
('immunization', 'Immunization', 'BOOLEAN', FALSE, TRUE, 17),
('height_cm', 'Height', 'NUMBER', FALSE, TRUE, 18),
('weight_kg', 'Weight', 'NUMBER', FALSE, TRUE, 19),
('vision_check', 'Vision', 'STRING', FALSE, TRUE, 20),
('character_cert_url', 'Character Cert', 'FILE_URL', FALSE, TRUE, 21),
('fee_status', 'Fee Status', 'STRING', FALSE, TRUE, 22),
('attendance_percent', 'Attendance %', 'NUMBER', FALSE, TRUE, 23),
('udise_uploaded', 'UDISE Uploaded', 'BOOLEAN', FALSE, TRUE, 24),
('father_name', 'Father Name', 'STRING', FALSE, TRUE, 25),
('father_contact', 'Father Contact', 'STRING', TRUE, TRUE, 26),
('father_aadhaar', 'Father Aadhaar', 'STRING', FALSE, TRUE, 27),
('mother_name', 'Mother Name', 'STRING', FALSE, TRUE, 28),
('mother_contact', 'Mother Contact', 'STRING', TRUE, TRUE, 29),
('mother_aadhaar', 'Mother Aadhaar', 'STRING', FALSE, TRUE, 30),
('guardian_name', 'Guardian Name', 'STRING', FALSE, TRUE, 31),
('guardian_contact', 'Guardian Contact', 'STRING', FALSE, TRUE, 32),
('guardian_relation', 'Guardian Relation', 'STRING', FALSE, TRUE, 33),
('guardian_aadhaar', 'Guardian Aadhaar', 'STRING', FALSE, TRUE, 34),
('family_status', 'Family Status', 'STRING', FALSE, TRUE, 35),
('language_preference', 'Language', 'STRING', FALSE, TRUE, 36);
