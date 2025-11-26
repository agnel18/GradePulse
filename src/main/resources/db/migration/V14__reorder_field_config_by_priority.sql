-- V14: Reorder field_config by priority and update display names

-- Core identity and essentials
UPDATE field_config SET sort_order = 1,  display_name = 'Student ID' WHERE field_name = 'student_id';
UPDATE field_config SET sort_order = 2,  display_name = 'Full Name' WHERE field_name = 'full_name';
UPDATE field_config SET sort_order = 3,  display_name = 'DOB (DD/MM/YYYY)' WHERE field_name = 'date_of_birth';
UPDATE field_config SET sort_order = 4,  display_name = 'Gender' WHERE field_name = 'gender';
UPDATE field_config SET sort_order = 5,  display_name = 'APAAR ID (Automated Permanent Academic Account Registry)' WHERE field_name = 'apaar_id';
UPDATE field_config SET sort_order = 6,  display_name = 'Student Aadhaar' WHERE field_name = 'aadhaar_number';
UPDATE field_config SET sort_order = 7,  display_name = 'Category' WHERE field_name = 'category';
UPDATE field_config SET sort_order = 8,  display_name = 'Address' WHERE field_name = 'address';
UPDATE field_config SET sort_order = 9,  display_name = 'Photo' WHERE field_name = 'photo_url';
UPDATE field_config SET sort_order = 10, display_name = 'Transfer Certificate (TC)' WHERE field_name = 'previous_school_tc_url';
UPDATE field_config SET sort_order = 11, display_name = 'Admission Class' WHERE field_name = 'admission_class';
UPDATE field_config SET sort_order = 12, display_name = 'Admission Date (DD/MM/YYYY)' WHERE field_name = 'admission_date';
UPDATE field_config SET sort_order = 13, display_name = 'Enrollment No' WHERE field_name = 'enrollment_no';
UPDATE field_config SET sort_order = 14, display_name = 'Marksheet' WHERE field_name = 'previous_marksheet_url';
UPDATE field_config SET sort_order = 15, display_name = 'Blood Group' WHERE field_name = 'blood_group';
UPDATE field_config SET sort_order = 16, display_name = 'Character Certificate' WHERE field_name = 'character_cert_url';

-- System-calculated / admin flags (kept in requested order)
UPDATE field_config SET sort_order = 17, display_name = 'Fee Status' WHERE field_name = 'fee_status';
UPDATE field_config SET sort_order = 18, display_name = 'Attendance %' WHERE field_name = 'attendance_percent';
UPDATE field_config SET sort_order = 19, display_name = 'UDISE Uploaded (Unified District Information System for Education)' WHERE field_name = 'udise_uploaded';

-- Parent/Guardian
UPDATE field_config SET sort_order = 20, display_name = 'Father Name' WHERE field_name = 'father_name';
UPDATE field_config SET sort_order = 21, display_name = 'Father Contact' WHERE field_name = 'father_contact';
UPDATE field_config SET sort_order = 22, display_name = 'Father Aadhaar' WHERE field_name = 'father_aadhaar';
UPDATE field_config SET sort_order = 23, display_name = 'Mother Name' WHERE field_name = 'mother_name';
UPDATE field_config SET sort_order = 24, display_name = 'Mother Contact' WHERE field_name = 'mother_contact';
UPDATE field_config SET sort_order = 25, display_name = 'Mother Aadhaar' WHERE field_name = 'mother_aadhaar';
UPDATE field_config SET sort_order = 26, display_name = 'Guardian Name' WHERE field_name = 'guardian_name';
UPDATE field_config SET sort_order = 27, display_name = 'Guardian Contact' WHERE field_name = 'guardian_contact';
UPDATE field_config SET sort_order = 28, display_name = 'Guardian Relation' WHERE field_name = 'guardian_relation';
UPDATE field_config SET sort_order = 29, display_name = 'Guardian Aadhaar' WHERE field_name = 'guardian_aadhaar';
UPDATE field_config SET sort_order = 30, display_name = 'Family Status' WHERE field_name = 'family_status';
UPDATE field_config SET sort_order = 31, display_name = 'Language' WHERE field_name = 'language_preference';

-- Class and School context
UPDATE field_config SET sort_order = 32, display_name = 'Class' WHERE field_name = 'student_class';
UPDATE field_config SET sort_order = 33, display_name = 'Division/Stream' WHERE field_name = 'division';
UPDATE field_config SET sort_order = 34, display_name = 'Sub-Division/Section' WHERE field_name = 'sub_division';
UPDATE field_config SET sort_order = 35, display_name = 'School Name' WHERE field_name = 'school_name';
UPDATE field_config SET sort_order = 36, display_name = 'Board' WHERE field_name = 'board';
UPDATE field_config SET sort_order = 37, display_name = 'Current Academic Year' WHERE field_name = 'academic_year';
