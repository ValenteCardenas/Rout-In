# Rout-In MVP: Lessons Learned & Compilation Best Practices

This document compiles the technical lessons and strict coding constraints discovered during the development and compilation of the Rout-In MVP. All future developer agents (including Antigravity) must read and enforce these rules to prevent compilation and runtime errors on the target platform.

---

## 1. Android Resource Naming Rules (Critical)

Android's resource merger enforces strict naming conventions for all files inside the `res/` directory.

* **The Rule:** File-based resource names (drawables, layouts, values, XMLs, fonts, assets) **must contain only lowercase a-z, 0-9, or underscores (`_`)**.
* **What Failed:** The font asset `Comfortaa-Bold.ttf` violated this by using capital letters and a hyphen.
* **The Solution:** Always rename assets to lowercase with underscores before reference (e.g., `comfortaa_bold.ttf`).

---

## 2. XML Prolog Placement

The XML parser built into the Android Gradle Plugin (AGP) resource parser is extremely strict regarding the placement of the XML declaration header.

* **The Rule:** The `<?xml version="1.0" encoding="utf-8"?>` prolog **MUST be the absolute first line of the file (character index 0)**. No whitespace, blank lines, or comments are allowed before it.
* **What Failed:** Multiple vector drawables and mipmap XMLs had license comment blocks placed *before* the XML prolog.
* **The Solution:** Ensure the prolog is always the very first line. Example:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <!-- License comments go here, AFTER the prolog -->
  <vector ...>
  ```

---

## 3. Material Icons Extended Library Limitations

The Compose `material-icons-extended` library does not include brand-specific icons (like Google, Apple, Facebook, etc.).

* **The Rule:** Do **not** attempt to reference `Icons.Rounded.Google` or other brand icons directly from `androidx.compose.material.icons`.
* **The Solution:** Use standard semantic stand-ins (such as `Icons.Rounded.AccountCircle` for accounts, or `Icons.Rounded.Mail` for general email/outlook) or import raw custom vector paths if a precise brand representation is required.

---

## 4. Mutable State Fields in Models

Kotlin data models mapped to state machine engines must be modeled with mutability in mind.

* **The Rule:** Fields that undergo scheduled updates, status changes, or chronological re-allocations (such as `scheduledTime` or `status` in the `HabitBlock` model) must be defined as `var` instead of `val`.
* **What Failed:** `val scheduledTime` caused compiler re-assignment errors inside the ViewModel when executing voice shifts and calendar reconciliations.
* **The Solution:** Carefully demarcate mutable fields as `var` during initial model declaration:
  ```kotlin
  data class HabitBlock(
      val id: Int,
      val name: String,
      var scheduledTime: String, // Must be mutable for schedule shift logic
      val durationMinutes: Int,
      var status: String         // Must be mutable for friction state shifts
  )
  ```

---

## 5. Non-Deprecated API Usage (Locale API)

Avoid using deprecated constructors from Java standard libraries to ensure clean build logs and forward compatibility.

* **The Rule:** Do **not** use the deprecated `Locale(String language, String country)` constructor.
* **The Solution:** Use the non-deprecated factory method `Locale.forLanguageTag(String languageTag)` instead.
  * *Deprecated:* `Locale("es", "MX")`
  * *Modern:* `Locale.forLanguageTag("es-MX")`
