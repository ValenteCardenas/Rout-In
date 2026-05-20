# Rout-In MVP: UI Design System & Jetpack Compose Theme Specification

This document translates the official **Rout-In Identity Manual** into programmatic design tokens and layout constraints for Jetpack Compose. The agéntic IDE (**Antigravity**) must strictly implement these colors, typography rules, and shape philosophies to ensure visual consistency and maximize the project's Usability and UI/UX scores.

---

## 1. Visual Intent & Brand Philosophy
The visual core of Rout-In is designed to project **calm, accessibility, friendliness, emotional closeness, and trust**. It deliberately rejects rigid, cold corporate designs as well as overly childish semantics. 
* **Design Goal:** Reduce the subconscious anxiety, pressure, and stress typically associated with productivity tracking and rigid scheduling tools. 
* **Form Factor:** All UI elements must lean heavily on rounded, smooth, and soft edges to foster a sense of reassurance and behavioral accompaniment.

---

## 2. Color Palette Jetpack Compose Token Mapping

The color palette utilizes high-contrast clear tones paired with deep dark tones to maintain high accessibility, premium readability, and prevent visual fatigue during long usage sessions.

### 2.1 Programmatic Color Definitions
```kotlin
package com.uam.routin.ui.theme

import androidx.compose.ui.graphics.Color

object RoutInColors {
    // --- Primary Darks (Core Backgrounds & Surface Containers) ---
    val DeepPurpleNavy = Color(0xFF1C1B29) // Main background frame (#1c1b29)
    val DarkSurface = Color(0xFF262739)    // Core cards, lists, containers (#262739)
    val SlateGray = Color(0xFF33323E)       // Subtle headers, divider fields (#33323e)

    // --- Primary Lights (High-Contrast Text & Clean Accents) ---
    val SoftMutedLavender = Color(0xFFADADC0) // Secondary body text, unselected icons (#adadc0)
    val OffWhiteSerenity = Color(0xFFD5DDDD)  // Primary titles, labels, emphasis typography (#d5dddd)

    // --- Secondary Pastels (Contextual Behavior & State Cards) ---
    val ClarityBlue = Color(0xFFD2E4F8)       // Used for standard tasks, mental clarity (#d2e4f8)
    val WellbeingMint = Color(0xFFDCF5EC)     // Used for habits, health, personal growth (#dcf5ec)
    val OptimismYellow = Color(0xFFFFF5CC)    // Used for warnings, reminders, motivational hints (#fff5cc)

    // --- Core Action Accent ---
    val VibrantGreenEmphasis = Color(0xFF7CD4B0) // Success states, task completions, buttons (#7cd4b0)
}

```

### 2.2 Material 3 Dark Scheme Integration

```kotlin
import androidx.compose.material3.darkColorScheme

val RoutInDarkColorScheme = darkColorScheme(
    primary = RoutInColors.VibrantGreenEmphasis,
    onPrimary = RoutInColors.DeepPurpleNavy,
    background = RoutInColors.DeepPurpleNavy,
    onBackground = RoutInColors.OffWhiteSerenity,
    surface = RoutInColors.DarkSurface,
    onSurface = RoutInColors.OffWhiteSerenity,
    surfaceVariant = RoutInColors.SlateGray,
    onSurfaceVariant = RoutInColors.SoftMutedLavender
)

```

---

## 3. Typography Specification

The font asset selection directly reinforces proximity and simplicity through smooth, curved geometry.

* **Selected Typeface:** `Comfortaa Bold`
* **Design Rationale:** Its highly readable, rounded geometry delivers immediate emotional proximity, removing the rigid coldness of standard system sans-serif typefaces.

### Compose Typography Implementation Guide:

```kotlin
package com.uam.routin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.uam.routin.R

val ComfortaaFontFamily = FontFamily(
    Font(R.font.comfortaa_bold, FontWeight.Bold)
)

val RoutInTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = ComfortaaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ComfortaaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ComfortaaFontFamily,
        fontWeight = FontWeight.Bold, // Maintain bold hierarchy as per identity profile
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ComfortaaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

```

---

## 4. Component Design Rules & Structural Hierarchy

When generating layout composables, the compiler must stick to these concrete element rules:

### 4.1 Card Component Layouts (`HabitBlock` Rendering)

* **Shape Corners:** Card containers must use extreme roundness (`shape = RoundedCornerShape(24.dp)`). Renders a smooth, friendly geometry.
* **Background Shading:**
* Regular habits in list view use `RoutInColors.DarkSurface`.
* Active/Interactive state modifications use background card accents: `ClarityBlue` (standard actions), `WellbeingMint` (habits), or `OptimismYellow` (friction triggers). When a pastel accent color is used as a card background, typography colors must swap automatically to `DeepPurpleNavy` to preserve strict accessibility contrast.



### 4.2 Interaction Buttons & Floating Action Buttons (FAB)

* **Primary Audio FAB:** The voice capture button must be fully circular, colored in `VibrantGreenEmphasis`.
* **State Feedback:** When a task status changes to "COMPLETED", use `VibrantGreenEmphasis` to color the visual completion indicators, conveying reward, growth, and task resolution.