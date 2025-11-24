# GradePulse — Free Forever Grade Tracker

**"No school should pay ₹50,000/year to tell parents their child got 19/20"**

Live: (coming soon)  
GitHub: https://github.com/agnel18/GradePulse

---

## Pain Points We Solve

| For Teachers | For Parents |
|-------------|------------|
| 2+ hours entering grades → **2 minutes with Excel** | No update → **WhatsApp in <3 seconds** |
| Manual attendance → **One tap, 500 parents notified** | No syllabus view → **Chapter-wise % on phone** |
| No IT support → **Zero install, free hosting** | 50MB app → **Works on ₹2999 JioPhone** |

---

## Project Status (Last Updated: Nov 24, 2025)

### ✅ Completed Features

#### 1. **Dynamic Field Configuration System**
- 36 configurable core fields (Student ID, Name, DOB, Contact info, etc.)
- Toggle fields ON/OFF based on school requirements
- Drag-and-drop field reordering with visual grip icons
- Custom field support (add school-specific fields)
- User-friendly display names with metric units (cm, kg) and DD/MM/YYYY date format
- Real-time field count (Core: 36, Custom: X)

#### 2. **Smart Excel Upload & Template System**
- **Dynamic column parser** - reads Excel headers and maps to field names
- Handles any field configuration (active/inactive, reordered)
- Auto-generates template with only active fields
- Pre-formatted TEXT columns (100 rows) to prevent Excel auto-formatting issues
- Skips empty rows automatically
- Phone number normalization (handles +, 00, dashes, spaces, dots)
- Required field indicators (Student ID *, Full Name *)

#### 3. **Data Validation & Preview**
- Upload validation with detailed error messages
- Preview table with improved column spacing
- Error column with text wrapping for readability
- Compare uploaded data with existing database records
- Highlight changed fields with old vs new values
- Bulk import with validation summary

#### 4. **Attendance Management**
- One-tap attendance marking by class section
- Mark entire class present/absent in seconds
- Individual student attendance override
- Daily attendance records with date tracking
- WhatsApp alerts for absent students to parents
- Attendance percentage tracking per student

#### 5. **WhatsApp Integration**
- Twilio sandbox setup and configuration
- Attendance absence alerts to parents
- Country code validation (+971, +91, etc.)
- Phone number format validation and normalization
- Alert history tracking

#### 6. **Security & Authentication**
- Spring Security with form-based login
- Password encryption (BCrypt)
- In-memory user store (ready for database upgrade)
- CSRF protection with selective endpoint exclusions
- H2 console access for development

#### 7. **Database & Migrations**
- H2 in-memory database (development)
- Flyway migration management (6 migrations)
- Student master data table
- Field configuration table
- Attendance records table
- Class section management
- WhatsApp alert history

#### 8. **UI/UX Improvements**
- Bootstrap 5 responsive design
- Mobile-first approach
- Dashboard with stats cards (white text fix)
- Color-coded field type badges (Text, Number, Date, Yes/No, File Link)
- Toggle buttons with ON/OFF visual feedback (green/gray)
- Simplified modals for non-technical users
- Professional field naming (no abbreviations)
- Acronym explanations (APAAR ID, UDISE)

### 🚧 In Progress / Pending Features

#### High Priority
- [ ] Production database setup (MySQL/PostgreSQL)
- [ ] User management with roles (Admin, Teacher, Principal)
- [ ] Grade entry and management system
- [ ] Report card generation
- [ ] Dashboard analytics and charts
- [ ] Bulk WhatsApp messaging for grades

#### Medium Priority
- [ ] Syllabus tracker (chapter-wise completion %)
- [ ] GPA trend graphs (Chart.js)
- [ ] Fee management module
- [ ] Module toggle system (enable/disable features)
- [ ] Student search and filtering
- [ ] Export data to Excel

#### Future Enhancements
- [ ] PWA support (offline capability)
- [ ] Multi-language support
- [ ] Parent portal (view-only access)
- [ ] SMS fallback for WhatsApp failures
- [ ] Automated backup system
- [ ] Cloud deployment (Railway.app)
- [ ] Multi-school support (tenant isolation)
- [ ] Academic year management

---

## Current Capabilities

**What You Can Do Today:**
1. ✅ Upload student data via Excel (dynamic field mapping)
2. ✅ Configure which fields to use (activate/deactivate)
3. ✅ Reorder fields using drag-and-drop
4. ✅ Mark attendance for entire class in one tap
5. ✅ Send WhatsApp alerts for absent students
6. ✅ View student records with attendance percentage
7. ✅ Preview and validate uploaded data before saving
8. ✅ Track attendance history by date and class

**What's Coming Next:**
- Grade entry and report card generation
- Parent portal for viewing grades
- Dashboard with analytics and trends
- Production deployment on Railway.app

---

## Tech Stack

**Backend:**
- Spring Boot 3.4.11 (Java 25)
- Spring Security (BCrypt authentication)
- Spring Data JPA (Hibernate)
- H2 Database (development) / MySQL (planned for production)
- Flyway (database migrations)

**Frontend:**
- Thymeleaf (server-side templates)
- Bootstrap 5.3 (responsive UI)
- Font Awesome 6.7 (icons)
- Chart.js (planned for analytics)
- HTML5 Drag & Drop API

**Integrations:**
- Apache POI 5.x (Excel generation/parsing)
- Twilio WhatsApp API (parent notifications)
- Lombok (code generation)
- Gson (JSON processing)

**DevOps:**
- Maven 3.9.11 (build tool)
- Git (version control)
- Railway.app (planned for free hosting)
- H2 Console (database management)

---

## Author

**Agnel J N**  
Full Stack Developer | Bengaluru  
---

## Master Plan
See [`docs/master-plan.md`](./docs/master-plan.md)