# GradePulse - Document Upload Guide

## Phase 1 Complete: Document URL Support ✅

### Changes Made:

1. **Database Schema Updated** (V3__document_urls.sql):
   - `photo` → `photo_url` (TEXT)
   - `previous_school_tc` → `previous_school_tc_url` (TEXT)
   - `previous_marksheet` → `previous_marksheet_url` (TEXT)
   - `character_cert` → `character_cert_url` (TEXT)
   - **NEW**: `aadhaar_card_url` (TEXT)

2. **Student Model Updated**:
   - All document fields now store URLs instead of boolean values
   - Supports Google Drive, Dropbox, OneDrive, or any shareable link

3. **Excel Template Format**:
   ```
   Column 8  (Photo):              https://drive.google.com/file/d/xxx/view
   Column 9  (TC):                 https://drive.google.com/file/d/yyy/view
   Column 13 (Marksheet):          https://drive.google.com/file/d/zzz/view
   Column 20 (Character Cert):     https://drive.google.com/file/d/aaa/view
   ```

### How Schools Use This:

1. **Upload documents to Google Drive** (or any cloud storage)
2. **Get shareable link** (Anyone with link can view)
3. **Paste URL in Excel** in the appropriate column
4. **Import to GradePulse** - URLs stored in database
5. **View anytime** - Click URL to open document

### Supported File Types:
- **Images**: JPEG, PNG, GIF (for photos)
- **Documents**: PDF (for certificates, TC, marksheet, aadhaar)

---

## Next: Phase 2 - Field Builder UI

Create `/fields` page where schools can:
- ✅ Add custom fields (e.g., "Caste Certificate URL", "Bus Route", "Blood Donation Consent")
- ✅ Choose field type: TEXT, NUMBER, DATE, BOOLEAN, **FILE_URL**
- ✅ Mark as required/optional
- ✅ Activate/deactivate fields
- ✅ Reorder fields
- ✅ Custom fields stored in `dynamic_data` JSON column

---

## Example: School Customization

### School A (Government School):
- Adds: "Caste Certificate URL" (FILE_URL)
- Adds: "Income Certificate URL" (FILE_URL)
- Hides: "Character Certificate" (not needed)

### School B (Private School):
- Adds: "NRI Quota" (BOOLEAN)
- Adds: "Sibling Enrollment No" (TEXT)
- Adds: "Medical Insurance Card URL" (FILE_URL)

Same codebase, infinite flexibility! 🚀
