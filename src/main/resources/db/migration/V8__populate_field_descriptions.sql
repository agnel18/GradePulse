-- V8__populate_field_descriptions.sql
-- Populate accurate, neutral descriptions for the 36 core fields
-- Adjusts for mismatch between earlier V7 naming and actual field_name values from V4

-- Safety: only run if description column exists
-- (H2 will ignore this check style; assuming column present after V7)

UPDATE field_config SET description = 'Unique school-issued identifier (e.g., STU2025-001). Used in all academic and administrative records.' WHERE field_name = 'student_id';
UPDATE field_config SET description = 'Student''s full legal name as per admission records.' WHERE field_name = 'full_name';
UPDATE field_config SET description = 'Date of birth in DD/MM/YYYY format (used for age calculation).' WHERE field_name = 'date_of_birth';
UPDATE field_config SET description = 'Gender as recorded on official documents (Male / Female).' WHERE field_name = 'gender';
UPDATE field_config SET description = 'APAAR ID (national academic registry identifier) if available.' WHERE field_name = 'apaar_id';
UPDATE field_config SET description = 'Student Aadhaar number (official national ID) if collected.' WHERE field_name = 'aadhaar_number';
UPDATE field_config SET description = 'Government or school admission category (e.g., General, OBC, SC, ST) if applicable.' WHERE field_name = 'category';
UPDATE field_config SET description = 'Current residential address (house / street / locality).' WHERE field_name = 'address';
UPDATE field_config SET description = 'Passport-size photo or profile image file link.' WHERE field_name = 'photo_url';
UPDATE field_config SET description = 'Transfer Certificate document link from previous school.' WHERE field_name = 'previous_school_tc_url';
UPDATE field_config SET description = 'Class/grade at the time of admission (e.g., Grade 4).' WHERE field_name = 'admission_class';
UPDATE field_config SET description = 'Admission date in DD/MM/YYYY format.' WHERE field_name = 'admission_date';
UPDATE field_config SET description = 'Enrollment or registration number assigned by the institution.' WHERE field_name = 'enrollment_no';
UPDATE field_config SET description = 'Previous academic year marksheet document link.' WHERE field_name = 'previous_marksheet_url';
UPDATE field_config SET description = 'Blood group (e.g., A+, O-, AB+) for health and emergency reference.' WHERE field_name = 'blood_group';
UPDATE field_config SET description = 'Known allergies or medical conditions (short summary).' WHERE field_name = 'allergies_conditions';
UPDATE field_config SET description = 'Indicates whether immunization record is verified (Yes / No).' WHERE field_name = 'immunization';
UPDATE field_config SET description = 'Height measured in centimeters.' WHERE field_name = 'height_cm';
UPDATE field_config SET description = 'Weight measured in kilograms.' WHERE field_name = 'weight_kg';
UPDATE field_config SET description = 'Vision screening result (e.g., 6/6 or diopter value).' WHERE field_name = 'vision_check';
UPDATE field_config SET description = 'Character certificate document link if provided.' WHERE field_name = 'character_cert_url';
UPDATE field_config SET description = 'Current fee payment or clearance status (e.g., Paid, Pending, Partial).' WHERE field_name = 'fee_status';
UPDATE field_config SET description = 'Attendance percentage for the current academic period.' WHERE field_name = 'attendance_percent';
UPDATE field_config SET description = 'Marks whether UDISE record has been uploaded (Yes / No).' WHERE field_name = 'udise_uploaded';
UPDATE field_config SET description = 'Father''s name as per admission form.' WHERE field_name = 'father_name';
UPDATE field_config SET description = 'Primary contact number for father.' WHERE field_name = 'father_contact';
UPDATE field_config SET description = 'Father''s Aadhaar number if collected.' WHERE field_name = 'father_aadhaar';
UPDATE field_config SET description = 'Mother''s name as per admission form.' WHERE field_name = 'mother_name';
UPDATE field_config SET description = 'Primary contact number for mother.' WHERE field_name = 'mother_contact';
UPDATE field_config SET description = 'Mother''s Aadhaar number if collected.' WHERE field_name = 'mother_aadhaar';
UPDATE field_config SET description = 'Guardian name (if different from parents).' WHERE field_name = 'guardian_name';
UPDATE field_config SET description = 'Guardian contact number.' WHERE field_name = 'guardian_contact';
UPDATE field_config SET description = 'Relationship of guardian to student (e.g., Uncle, Aunt, Grandparent).' WHERE field_name = 'guardian_relation';
UPDATE field_config SET description = 'Guardian Aadhaar number if collected.' WHERE field_name = 'guardian_aadhaar';
UPDATE field_config SET description = 'Basic family background or living situation summary.' WHERE field_name = 'family_status';
UPDATE field_config SET description = 'Primary language preference for communication (e.g., English, Hindi).' WHERE field_name = 'language_preference';

-- Ensure no NULL descriptions remain (fallback if any still blank)
UPDATE field_config SET description = 'Field description pending review.' WHERE (description IS NULL OR TRIM(description) = '');
