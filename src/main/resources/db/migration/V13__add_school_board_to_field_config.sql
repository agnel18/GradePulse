-- V13: Ensure School Name, Board, Academic Year appear in Field Builder and Template
-- Insert if missing; update display names and sort order

MERGE INTO field_config (field_name, display_name, field_type, required, active, sort_order)
KEY(field_name)
VALUES
    ('school_name', 'School Name', 'STRING', TRUE, TRUE, 40),
    ('board', 'Board', 'STRING', TRUE, TRUE, 41),
    ('academic_year', 'Academic Year', 'STRING', TRUE, TRUE, 42);
