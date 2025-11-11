-- V2: Add family and guardian fields (H2 compatible)
ALTER TABLE students ADD COLUMN father_name VARCHAR(255);
ALTER TABLE students ADD COLUMN father_contact VARCHAR(20);
ALTER TABLE students ADD COLUMN father_aadhaar VARCHAR(20);
ALTER TABLE students ADD COLUMN mother_name VARCHAR(255);
ALTER TABLE students ADD COLUMN mother_contact VARCHAR(20);
ALTER TABLE students ADD COLUMN mother_aadhaar VARCHAR(20);
ALTER TABLE students ADD COLUMN guardian_name VARCHAR(255);
ALTER TABLE students ADD COLUMN guardian_contact VARCHAR(20);
ALTER TABLE students ADD COLUMN guardian_aadhaar VARCHAR(20);
ALTER TABLE students ADD COLUMN guardian_relation VARCHAR(50);
ALTER TABLE students ADD COLUMN family_status VARCHAR(50) DEFAULT 'Both Parents';