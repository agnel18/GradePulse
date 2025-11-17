-- V3: Convert document fields to store URLs instead of boolean values

-- Change photo from BOOLEAN to TEXT (for storing URL)
ALTER TABLE students
    DROP COLUMN photo;

ALTER TABLE students
    ADD COLUMN photo_url TEXT;

-- Change previous_school_tc from BOOLEAN to TEXT
ALTER TABLE students
    DROP COLUMN previous_school_tc;

ALTER TABLE students
    ADD COLUMN previous_school_tc_url TEXT;

-- Change previous_marksheet from BOOLEAN to TEXT
ALTER TABLE students
    DROP COLUMN previous_marksheet;

ALTER TABLE students
    ADD COLUMN previous_marksheet_url TEXT;

-- Change character_cert from VARCHAR to TEXT (already exists, just rename for clarity)
ALTER TABLE students
    DROP COLUMN character_cert;

ALTER TABLE students
    ADD COLUMN character_cert_url TEXT;

-- Add aadhaar_card_url for storing aadhaar document
ALTER TABLE students
    ADD COLUMN aadhaar_card_url TEXT;

-- Add new field types to support FILE_URL in field_config
-- field_type can now be: STRING, NUMBER, DATE, BOOLEAN, FILE_URL
COMMENT ON COLUMN field_config.field_type IS 'STRING, NUMBER, DATE, BOOLEAN, FILE_URL';
