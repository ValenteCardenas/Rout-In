# Rout-In MVP: Technical Stack & Environment Specification

This document defines the strict, lightweight, and fully localized technical stack required to implement the simulated **Rout-In** MVP. Every component specified here must be utilized exclusively for UI state management, local animations, and hardware-level mock interactions. No remote infrastructure or live cloud API dependencies shall be introduced.

---

## 1. Core Development Environment & Build System

| Technology | Recommended Version | Purpose / Use Case in MVP |
| :--- | :--- | :--- |
| **Android Studio** | 2024.2.1 (Ladybug) or higher | Integrated Development Environment (IDE) used for writing, previewing, and deploying the native application to an emulator or physical test device. |
| **Android Gradle Plugin (AGP)** | 8.7.0 | Build automation tool used to compile the application resources, handle dependencies, and package the final debug APK. |
| **Gradle Wrapper** | 8.10 | Build execution environment paired with AGP to coordinate multi-module configuration and build lifecycle tasks. |
| **Java Development Kit (JDK)** | 21 (JetBrains Runtime) | The underlying runtime environment required by Android Studio and Gradle to compile modern Kotlin code. |
| **Minimum SDK (minSdk)** | API Level 26 (Android 8.0 Oreo) | Ensures compatibility with native vibration channels, advanced background workers, and modern UI rendering. |
| **Target SDK (targetSdk)** | API Level 35 (Android 15) | Declares compilation compliance against the latest Android operating system behaviors and security sandboxing policies. |

---

## 2. Programming Languages & Core UI Frameworks

| Technology | Recommended Version | Purpose / Use Case in MVP |
| :--- | :--- | :--- |
| **Kotlin Language** | 2.1.0 | Core programming language. Leveraged for its type safety, modern syntax, and native support for Coroutines and asynchronous state streaming. |
| **Jetpack Compose BOM** | 2026.04.00 (or latest stable) | Bill of Materials used to manage synchronized versions of Compose UI components. Renders the fluid, declarative user interface. |
| **Compose UI** | 1.7.0+ (via BOM) | Drawing engine for rendering layouts, applying Material Design 3 design tokens, and handling reactive user touch events. |
| **Compose Foundation** | 1.7.0+ (via BOM) | Provides internal building blocks like lazy lists, scrollable containers, and click modifiers required to build the flexible layout grid. |
| **Compose Material 3** | 1.3.0+ (via BOM) | Provides cutting-edge theme constructs, modern cards, Floating Action Buttons (FAB), interactive notifications, and dark-mode color structures. |

---

## 3. Architecture, State Management & Concurrency

| Technology | Recommended Version | Purpose / Use Case in MVP |
| :--- | :--- | :--- |
| **Jetpack Lifecycle ViewModel** | 2.8.5 | Architectural controller that retains and exposes the simulated application states (`MutableStateOf` / `StateFlow`). Survives configuration changes (e.g., screen rotation). |
| **Kotlin Coroutines Core** | 1.9.0 | Asynchronous execution framework. Essential for implementing the **`delay(1500)`** method which realistically models cloud network latency during voice interpretation. |
| **Kotlin Coroutines Android** | 1.9.0 | Extends coroutine dispatchers to ensure background processes (like the simulated timer or sync delta triggers) easily marshal back to the `Dispatchers.Main` UI thread. |

---

## 4. Native Android SDK Frameworks (Hardcoded Simulation Drivers)

Since this MVP completely abstracts cloud communication, it relies entirely on the pre-installed, high-performance APIs built natively into the Android operating system core. No external versioning or Gradle dependencies are required for these items.

| Framework API | Version | Purpose / Use Case in MVP |
| :--- | :--- | :--- |
| **`android.speech.tts.TextToSpeech`** | Native Android SDK | Invoked inside the ViewModel to synthesize local voice responses. Emulates the Cloud Text-to-Speech (TTS) layer by instantly reading the empirical text payload aloud upon microphone release. |
| **`android.app.NotificationManager`** | Native Android SDK | System service used to instantiate and display rich, interactive push notifications directly on the device's system tray during "Critical Friction" or "MCP Collision" events. |
| **`android.app.NotificationChannel`** | Native Android SDK | Required by Android 8.0+ to categorize notification behaviors, allowing the application to force high-priority visual pop-ups (banners) over other apps. |
| **`android.os.Vibrator` / `VibratorManager`** | Native Android SDK | Directly manipulates the physical device motor to execute custom, non-habituated haptic wave patterns, simulating the sensory-disruptive "anti-blindness" alert. |
| **`android.content.Intent` / `PendingIntent`** | Native Android SDK | Packages background operations within notification action buttons, allowing the user to trigger one-tap schedule approvals ("Move to 6:30 PM") directly from the lock screen. |

---

## 5. Summary of Prohibited Libraries (Explicit Exclusion for Agent)

To prevent code bloating or execution failures inside the agéntic IDE, the following frameworks **MUST NOT** be compiled or declared in the build files:
* **No Network Clients:** Do not include `Ktor Client`, `Retrofit`, or `OkHttp`.
* **No Database Systems:** Do not configure live `Room` compilation tasks or `SQLDelight` drivers. Local states should be mocked inside an in-memory `List` or `Map` data structure within the `ViewModel`.
* **No Cloud Identity / IA Engines:** Do not pull in `Supabase SDK`, `Firebase Auth`, `OpenAI Client`, or `Anthropic SDK`. All responses are strictly pre-baked inside local String resources.