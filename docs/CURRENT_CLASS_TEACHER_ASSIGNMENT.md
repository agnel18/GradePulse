# Current Class Tracking & Teacher Assignment

## ✅ Phase 1: Current Class Field (COMPLETED)

### Problem Identified
- Students only had `admission_class` field (class when they first joined)
- No way to track current class after yearly promotions
- Attendance system was using `admission_class` which becomes incorrect after promotion
- No teacher assignment system

### Solution Implemented

#### 1. Database Changes (V6 Migration)
```sql
ALTER TABLE students ADD COLUMN current_class VARCHAR(255);
UPDATE students SET current_class = admission_class WHERE current_class IS NULL;
```

#### 2. Entity Updates
- **Student.java**: Added `currentClass` field with getters/setters
- **StudentRepository.java**: Added `findByCurrentClass(String currentClass)` method
- **AttendanceService.java**: Changed from `findByAdmissionClass` to `findByCurrentClass`

#### 3. Upload System Updates
- **StudentUploadDto.java**: Added `currentClass` field
- **UploadController.java**: 
  - Parses `current_class` from Excel column 11
  - Updated all subsequent column indices (+1 shift)
  - Added field comparison logic for change tracking
  - Defaults to `admission_class` if `current_class` not provided

#### 4. Template Changes Required
**Excel template columns (NEW ORDER):**
| Column | Field Name | Description |
|--------|------------|-------------|
| A | student_id | Unique ID |
| B | full_name | Student name |
| C | date_of_birth | DOB |
| ... | ... | ... |
| K | admission_class | Class when first joined (NEVER CHANGES) |
| **L** | **current_class** | **Current class (UPDATE YEARLY)** |
| M | admission_date | Admission date |
| N | enrollment_no | Enrollment number |
| ... | ... | (All shifted by 1 column) |

### How It Works Now

1. **Initial Upload**: 
   - Student joins in 5th grade
   - `admission_class = "5th"` (permanent historical record)
   - `current_class = "5th"` (will be updated yearly)

2. **Next Year (Promotion)**:
   - Re-upload students with updated `current_class = "6th"`
   - `admission_class` stays "5th" (historical record preserved)
   - Attendance system uses `current_class` for marking

3. **Attendance Marking**:
   - Teacher selects class section (e.g., "6th Grade Section A")
   - System fetches students WHERE `current_class = "6th"`
   - Shows correct students for that class

---

## 🔜 Phase 2: Teacher Assignment System (NEXT FEATURE)

### Requirements

Based on your use case:
- Biology teacher teaches 5 different classes per day
- Each class has specific time slots
- Need role-based access control
- Teachers should only see their assigned classes

### Proposed Solution

#### 1. New Entities

**User.java** (Replace in-memory user with database users)
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    
    private String email;
    private String password; // BCrypt
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // ADMIN, TEACHER, PRINCIPAL
    
    private Boolean isActive;
    private LocalDateTime createdAt;
}

enum UserRole {
    ADMIN,      // Full access
    TEACHER,    // Can mark attendance for assigned classes
    PRINCIPAL   // View-only access to all data
}
```

**TeacherAssignment.java**
```java
@Entity
public class TeacherAssignment {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User teacher;
    
    @ManyToOne
    private ClassSection classSection;
    
    private String subject; // "Biology", "Mathematics", etc.
    
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, etc.
    
    private LocalTime startTime; // 08:00
    private LocalTime endTime;   // 09:00
    
    private String academicYear; // "2024-2025"
    private Boolean isActive;
    
    @UniqueConstraint on (teacher_id, class_section_id, day_of_week, start_time)
}
```

#### 2. New Features

**Admin Dashboard** (`/admin/teachers`)
- Create teacher accounts
- Assign teachers to class sections
- Define weekly timetable
- View teacher schedules

**Teacher Dashboard** (`/teacher/dashboard`)
- View assigned classes
- Quick access to mark attendance for today's classes
- Shows time slots and upcoming classes

**Enhanced Attendance Controller**
```java
@GetMapping("/attendance/mark")
public String markAttendance(Principal principal, Model model) {
    User teacher = userRepository.findByEmail(principal.getName());
    
    if (teacher.getRole() == UserRole.TEACHER) {
        // Show only assigned classes for today
        List<TeacherAssignment> todayClasses = 
            assignmentRepository.findByTeacherAndDayOfWeek(
                teacher, 
                LocalDate.now().getDayOfWeek()
            );
        model.addAttribute("classes", todayClasses);
    } else if (teacher.getRole() == UserRole.ADMIN) {
        // Show all classes
        model.addAttribute("classes", classSectionRepository.findAll());
    }
    
    return "attendance-form";
}
```

#### 3. Security Updates

**SecurityConfig.java**
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")
    .requestMatchers("/attendance/mark").hasAnyRole("TEACHER", "ADMIN")
    .anyRequest().authenticated()
);
```

#### 4. Database Migrations Needed

- **V7__create_users_table.sql**: User accounts with roles
- **V8__create_teacher_assignments.sql**: Teacher-class mappings with timetable

#### 5. UI Pages Needed

- **teacher-management.html**: Admin creates/edits teachers
- **teacher-assignment.html**: Admin assigns teachers to classes
- **timetable-view.html**: Visual weekly timetable
- **teacher-dashboard.html**: Teacher's personalized homepage

### Example Teacher Schedule

**Mr. Sharma (Biology Teacher)**
| Day | Time | Class | Subject |
|-----|------|-------|---------|
| Monday | 08:00-09:00 | 5th A | Biology |
| Monday | 09:00-10:00 | 6th B | Biology |
| Monday | 10:30-11:30 | 10th C | Biology |
| Monday | 11:30-12:30 | 6th A | Biology |
| Monday | 13:00-14:00 | 10th D | Biology |

When Mr. Sharma logs in on Monday morning, he sees these 5 classes with quick "Mark Attendance" buttons next to each.

---

## Implementation Priority

### ✅ Completed
1. ✅ Current class tracking (V6 migration)
2. ✅ Updated attendance system to use current_class
3. ✅ Updated upload system with current_class column

### 🔄 Next Steps (Phase 2)
1. **Update Excel template** with current_class column
2. **Test promotion workflow** (upload students with updated current_class)
3. **Plan teacher assignment feature** (Phase 2)
4. Create user management system
5. Build teacher assignment UI
6. Implement role-based access control
7. Create teacher dashboard

---

## Yearly Promotion Workflow

### End of Academic Year Process

1. **Export Current Data**
   - Download all students from database
   - Spreadsheet has both `admission_class` and `current_class`

2. **Update Current Classes**
   ```
   If current_class = "Nursery" → Update to "LKG"
   If current_class = "5th" → Update to "6th"
   If current_class = "10th" → Update to "11th"
   ```

3. **Re-upload Students**
   - Upload updated spreadsheet
   - System updates only `current_class` field
   - `admission_class` remains unchanged (historical record)

4. **Update Class Sections**
   - Admin creates new class sections for new academic year
   - Example: "2025-2026 → CBSE → General → 6th → A"

5. **Reassign Teachers** (Phase 2)
   - Admin updates teacher assignments for new academic year
   - Biology teacher now teaches 6th grade students who were in 5th last year

---

## Benefits

### Current Implementation
✅ Accurate attendance tracking (uses current_class)
✅ Historical data preserved (admission_class never changes)
✅ Simple yearly promotion (just update current_class column)
✅ No data migration issues

### Future Teacher Assignment (Phase 2)
🔜 Role-based access control
🔜 Teachers see only their classes
🔜 Automated timetable management
🔜 Principal has oversight of all classes
🔜 Admin can assign/reassign teachers easily
🔜 Audit trail of who marked attendance
