-- V15: Add missing class/division/subdivision fields to field_config
-- These were added to students table in V10 but never added to field_config
-- This is why they don't appear in templates or upload mapping
-- Using INSERT OR UPDATE to handle if V14 already updated them

MERGE INTO field_config (field_name, display_name, field_type, required, active, sort_order, description)
KEY(field_name)
VALUES
    ('student_class', 'Class', 'STRING', FALSE, TRUE, 6, 'Student class/standard (e.g., 10, FYJC, LKG)'),
    ('division', 'Division/Stream', 'STRING', FALSE, TRUE, 7, 'Division or stream (e.g., Science, Commerce, General)'),
    ('sub_division', 'Sub-Division/Section', 'STRING', FALSE, TRUE, 8, 'Section within division (e.g., A, B, C)');

-- Ensure V14 sort_order and display_name are applied (in case V14 ran before V15)
UPDATE field_config SET sort_order = 6,  display_name = 'Class' WHERE field_name = 'student_class';
UPDATE field_config SET sort_order = 7,  display_name = 'Division/Stream' WHERE field_name = 'division';
UPDATE field_config SET sort_order = 8,  display_name = 'Sub-Division/Section' WHERE field_name = 'sub_division';
