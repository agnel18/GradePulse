# 📋 Excel Template Update Guide - CURRENT_CLASS Column

## ⚠️ IMPORTANT: Template Format Changed

The Excel upload template **MUST** be updated to include the new `current_class` column.

---

## Column Changes

### ❌ OLD Template (Before V6)
| Col | Field Name | Description |
|-----|------------|-------------|
| A | student_id | Unique ID |
| B | full_name | Student name |
| C | date_of_birth | DD-MM-YYYY |
| D | gender | Male/Female/Other |
| E | apaar_id | APAAR ID |
| F | aadhaar_number | 12-digit Aadhaar |
| G | category | General/OBC/SC/ST |
| H | address | Full address |
| I | photo_url | Photo URL |
| J | previous_school_tc_url | TC document URL |
| **K** | **admission_class** | **Class when first joined** |
| **L** | **admission_date** | **Date of admission** |
| M | enrollment_no | Enrollment number |
| ... | ... | ... |

### ✅ NEW Template (After V6)
| Col | Field Name | Description |
|-----|------------|-------------|
| A | student_id | Unique ID |
| B | full_name | Student name |
| C | date_of_birth | DD-MM-YYYY |
| D | gender | Male/Female/Other |
| E | apaar_id | APAAR ID |
| F | aadhaar_number | 12-digit Aadhaar |
| G | category | General/OBC/SC/ST |
| H | address | Full address |
| I | photo_url | Photo URL |
| J | previous_school_tc_url | TC document URL |
| K | admission_class | Class when first joined (NEVER CHANGE) |
| **L** | **current_class** | **Current class (UPDATE YEARLY)** ⭐ NEW |
| M | admission_date | Date of admission |
| N | enrollment_no | Enrollment number |
| O | previous_marksheet_url | Marksheet URL |
| P | blood_group | Blood group |
| Q | allergies_conditions | Allergies/conditions |
| R | immunization | Yes/No |
| S | height_cm | Height in cm |
| T | weight_kg | Weight in kg |
| U | vision_check | Vision status |
| V | character_cert_url | Character certificate URL |
| W | fee_status | Paid/Pending |
| X | attendance_percent | Attendance percentage |
| Y | udise_uploaded | Yes/No |
| Z | father_name | Father's name |
| AA | father_contact | +971XXXXXXXXX or +91XXXXXXXXXX |
| AB | father_aadhaar | Father's Aadhaar |
| AC | mother_name | Mother's name |
| AD | mother_contact | +971XXXXXXXXX or +91XXXXXXXXXX |
| AE | mother_aadhaar | Mother's Aadhaar |
| AF | guardian_name | Guardian's name |
| AG | guardian_contact | +971XXXXXXXXX or +91XXXXXXXXXX |
| AH | guardian_relation | Relation |
| AI | guardian_aadhaar | Guardian's Aadhaar |
| AJ | family_status | Single Father/Single Mother/Both Parents/Guardian |
| AK | language_preference | ENGLISH/HINDI/TAMIL/KANNADA |

---

## Key Differences

### Column K: `admission_class` (Historical Record)
- **Purpose**: Permanent record of which class student joined
- **Example**: Student joined in "5th" grade
- **Update Policy**: ❌ **NEVER CHANGE THIS VALUE**
- **Use Case**: Historical tracking, admission reports

### Column L: `current_class` (Active Status) ⭐ NEW
- **Purpose**: Current class after promotions
- **Example**: Student is now in "6th" grade (was "5th" last year)
- **Update Policy**: ✅ **UPDATE EVERY ACADEMIC YEAR**
- **Use Case**: Attendance marking, current class reports

---

## Example Data

### Year 1 (2024-2025) - Initial Admission
| student_id | full_name | admission_class | **current_class** | admission_date |
|------------|-----------|-----------------|-------------------|----------------|
| STU001 | Rahul Sharma | 5th | **5th** | 15-04-2024 |
| STU002 | Priya Patel | Nursery | **Nursery** | 10-06-2024 |
| STU003 | Amit Kumar | 10th | **10th** | 01-04-2024 |

### Year 2 (2025-2026) - After Promotion
| student_id | full_name | admission_class | **current_class** | admission_date |
|------------|-----------|-----------------|-------------------|----------------|
| STU001 | Rahul Sharma | 5th *(unchanged)* | **6th** ⬆️ | 15-04-2024 |
| STU002 | Priya Patel | Nursery *(unchanged)* | **LKG** ⬆️ | 10-06-2024 |
| STU003 | Amit Kumar | 10th *(unchanged)* | **11th** ⬆️ | 01-04-2024 |

### Year 3 (2026-2027) - After Another Promotion
| student_id | full_name | admission_class | **current_class** | admission_date |
|------------|-----------|-----------------|-------------------|----------------|
| STU001 | Rahul Sharma | 5th *(still unchanged)* | **7th** ⬆️⬆️ | 15-04-2024 |
| STU002 | Priya Patel | Nursery *(still unchanged)* | **UKG** ⬆️⬆️ | 10-06-2024 |
| STU003 | Amit Kumar | 10th *(still unchanged)* | **12th** ⬆️⬆️ | 01-04-2024 |

---

## What Happens in the System

### Attendance Marking
```
BEFORE (V5 - WRONG):
Teacher selects "6th Grade Section A"
System queries: SELECT * FROM students WHERE admission_class = '6th'
❌ PROBLEM: Gets students who joined in 6th, not students currently in 6th

AFTER (V6 - CORRECT):
Teacher selects "6th Grade Section A"
System queries: SELECT * FROM students WHERE current_class = '6th'
✅ CORRECT: Gets all students currently studying in 6th
```

### Reports
- **Admission Report**: Use `admission_class` (shows when they joined)
- **Current Enrollment**: Use `current_class` (shows current distribution)
- **Attendance**: Use `current_class` (mark attendance for current class)
- **Promotion History**: Compare `admission_class` vs `current_class` to see progress

---

## Migration Behavior

### If `current_class` is Empty/Missing
```java
// System automatically defaults to admission_class
s.setCurrentClass(dto.getCurrentClass() != null && !dto.getCurrentClass().isBlank() 
    ? dto.getCurrentClass() : dto.getAdmissionClass());
```

**Result**: If you upload old template without `current_class` column, system copies `admission_class` to `current_class`.

### For Existing Students in Database
```sql
-- V6 migration automatically populates current_class
UPDATE students SET current_class = admission_class WHERE current_class IS NULL;
```

**Result**: All existing students get their `admission_class` copied to `current_class`.

---

## How to Update Your Excel Template

### Option 1: Manual Update
1. Open your existing template
2. **Insert** a new column after `admission_class` (column K)
3. **Name** it `current_class`
4. **Copy** formula: `=K2` (copies admission_class value as default)
5. Drag down for all rows
6. For promotions, manually update `current_class` column

### Option 2: Automated Update (Recommended for Yearly Promotions)
```excel
=IF(K2="Nursery", "LKG",
 IF(K2="LKG", "UKG",
 IF(K2="UKG", "1st",
 IF(K2="1st", "2nd",
 IF(K2="2nd", "3rd",
 ... continue pattern ...
 IF(K2="11th", "12th", K2))))))
```

This formula auto-increments class based on admission_class. Adjust as needed.

---

## Validation Rules

### Column K: `admission_class`
- ✅ Valid: "5th", "Nursery", "10th", "LKG", "UKG", "1st", "2nd", etc.
- ❌ Invalid: Empty (will cause validation error)
- **Rule**: Must match one of the class_name values in class_sections table

### Column L: `current_class`
- ✅ Valid: Same as above
- ⚠️ If empty: System defaults to `admission_class`
- **Rule**: Must match one of the class_name values in class_sections table

---

## Frequently Asked Questions

### Q1: What if I upload the old template without `current_class`?
**A**: System will default `current_class` to `admission_class`. It will work but won't help with promotions.

### Q2: Do I need to update all old student records?
**A**: No. V6 migration already copied `admission_class` to `current_class` for all existing students.

### Q3: Can admission_class and current_class be different?
**A**: Yes! That's the whole point. `admission_class` = historical, `current_class` = present.

### Q4: What happens if current_class is wrong?
**A**: Wrong students will appear when marking attendance. Re-upload with corrected values.

### Q5: How do I promote all students at once?
**A**: 
1. Export all students
2. Update `current_class` column (increment by 1 year)
3. Keep `admission_class` unchanged
4. Re-upload the file

---

## Example Promotion Process

### End of Academic Year 2024-2025

**Step 1**: Export current students
```
student_id | full_name | admission_class | current_class
STU001 | Rahul | 5th | 5th
STU002 | Priya | 5th | 5th
STU003 | Amit | 5th | 5th
```

**Step 2**: Update `current_class` only
```
student_id | full_name | admission_class | current_class
STU001 | Rahul | 5th | 6th ← CHANGED
STU002 | Priya | 5th | 6th ← CHANGED
STU003 | Amit | 5th | 6th ← CHANGED
```

**Step 3**: Re-upload Excel file
- System updates only `current_class` field
- `admission_class` remains "5th" (never changes)

**Step 4**: Verify in system
- Attendance for "6th Grade" now shows Rahul, Priya, Amit
- Reports show they're in 6th grade
- Historical records show they joined in 5th grade

---

## Teacher Assignment (Coming in Phase 2)

Once teacher assignment is implemented:
- Teachers will see only students in their assigned `current_class`
- Biology teacher teaching "6th A" sees all students WHERE `current_class = '6th'`
- No manual filtering needed

---

## Support

If you face issues with the new column:
1. Check column order matches new template
2. Ensure `current_class` has valid values (same as class_sections)
3. Re-upload if data is incorrect
4. Contact admin for bulk updates
