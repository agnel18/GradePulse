-- V13: Ensure School Name, Board, Academic Year appear in Field Builder and Template
-- Insert if missing; update display names and sort order

MERGE INTO field_config (field_name, display_name, field_type, required, active, sort_order)
KEY(field_name)
VALUES
    ('school_name', 'School Name', 'STRING', TRUE, TRUE, 40),
    ('board', 'Board', 'STRING', TRUE, TRUE, 41),
    ('academic_year', 'Academic Year', 'STRING', TRUE, TRUE, 42);

-- Optional: provide descriptions for clarity
MERGE INTO field_config_description (field_name, description)
KEY(field_name)
VALUES
    ('school_name', 'Name of the specific school within the management group'),
    ('board', 'Educational board affiliation (CBSE, ICSE, SSC, HSC, etc.)'),
    ('academic_year', 'Current academic year in format YYYY-YYYY');
