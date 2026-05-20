# Rout-In MVP Developer Skill & Execution Directive

**Role:** Senior Android Software Engineer  
**Objective:** Iterate, build upon the existing foundation, and complete the source code for the "Rout-In" MVP tailored for the SIE 2026 Hackathon.

## 🚨 HIGH-LEVEL CRITICAL INSTRUCTION
Given that the baseline Android project is already initialized, you must integrate seamlessly into the existing structure and implement a self-contained, In-Memory State Machine MVVM architecture. Do **NOT** integrate actual production dependencies (no Supabase, no OpenAI, no external network protocols). The application must emulate real-world system latencies, voice interaction responses, and complex scheduling state mutations using purely local Kotlin constructs and native Android hardware APIs.

---

## 🛠️ TARGET CODE ARCHITECTURE & DIRECTORY STRUCTURE
You must implement or refine the following functional components, organizing them cleanly by package within the existing `app` module:

### 1. Design System (`ui/theme/Theme.kt`, `Color.kt`, `Type.kt`)
Complete implementation of the Material Design 3 design system tokens. Assume standard system fonts (`FontFamily.SansSerif`) or a structural placeholder for `Comfortaa` utilizing heavy weights (`FontWeight.Bold`) by default to match the brand identity profile. Implement the official palette:
- **DeepPurpleNavy (`0xFF1C1B29`)**: Master layout frame background.
- **DarkSurface (`0xFF262739`)**: Standard habit container cards.
- **SoftMutedLavender (`0xFFADADC0`) & OffWhiteSerenity (`0xFFD5DDDD`)**: High-contrast typography.
- **Contextual Pastels**: ClarityBlue (`0xFFD2E4F8`), WellbeingMint (`0xFFDCF5EC`), and OptimismYellow (`0xFFFFF5CC`). *(Note: When a pastel color is used as a card background, typography colors must swap automatically to DeepPurpleNavy to preserve readability contrast).*
- **VibrantGreenEmphasis (`0xFF7CD4B0`)**: Task completions, success rewards, and key action states.

### 2. Data Model (`data/model/HabitBlock.kt`)
Data class modelling the routine blocks containing fields: 
`id`, `name`, `scheduledTime` (HH:mm string format), `durationMinutes`, `status` ("PENDING", "COMPLETED", "FRICTION", "REALLOCATED", "PENDING_REALLOCATION"), `isImmutable` (Boolean), and `source` ("INTERNAL", "EXTERNAL"). 
Include a `MockDataSeed` object initialized with Gabriel's 3 baseline habits: 
1. *Clase de Arquitectura* (14:00) 
2. *Reading Block* (16:00) 
3. *Gym / Workout* (18:00)

### 3. State Machine Engine (`viewmodel/RoutInViewModel.kt`)
The central State Machine engine of the app. It must expose an observable reactive state (`StateFlow` or `MutableStateOf` streams) directly to the UI layer. It must handle the lifecycle-aware execution of the local `TextToSpeech` framework, implementing `TextToSpeech.OnInitListener` and explicitly freeing resources inside the `onCleared()` lifecycle method to prevent memory leaks.

### 4. Background Receiver (`receiver/NotificationActionReceiver.kt`)
A native `BroadcastReceiver` subclass designed to capture one-tap interactive notification taps ("Move to 6:30 PM", "Re-locate to 7:30 PM"), safely dismiss the target notification tray entry, and dispatch intent signals back to the ViewModel state engine.

### 5. UI Layer (`ui/screens/MainDashboardScreen.kt`)
A fully declarative Jetpack Compose screen rendering the chronological habit block feed. Component cards must feature extreme corner smoothing (`shape = RoundedCornerShape(24.dp)`). Include an accessible "Debug Dashboard" section on screen containing explicit testing simulation triggers.

### 6. Entry Point (`MainActivity.kt`)
The single-activity entry point. Configures the high-importance behavioral alert `NotificationChannel` immediately upon app startup and mounts the Compose content view layout.

---

## ⚡ AUTONOMOUS SIMULATION SKILLS (REQUIRED BEHAVIORS)

### [Skill 1: Deployment Onboarding]
- **UI Architecture:** Welcome screen displaying two Material Design 3 selection cards (Open-Source Self-Hosted vs Premium Cloud).
- **Simulation Flow:** Tapping either card immediately mutates a local state token inside the ViewModel and triggers a fluid navigation transition to the Main Dashboard without remote server lookups. *(Reference: `SPEC01.md`)*

### [Skill 2: Conversational Voice Modification]
- **Trigger:** A circular Floating Action Button (FAB) styled in VibrantGreenEmphasis. Pressing and releasing simulates the inbound phrase token: *"Tengo una junta con mi asesor de Proyecto de Investigación a las 5, mueve mis hábitos de la tarde"*.
- **Simulation Flow:**
  1. Set UI state to Loading, triggering a native wave animation component in Jetpack Compose to emulate active microphone capture.
  2. Launch a Kotlin Coroutine with an explicit non-blocking **`delay(1500)`** to accurately simulate cloud API inference latency.
  3. Execute the local `TextToSpeech` channel to vocalize aloud: *"Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."*
  4. Perform atomic data mutation: Inject a new immutable entry "Junta de Proyecto de Investigación" at 17:00, and shift the subsequent "Gym / Workout" from 18:00 to 19:00 automatically with a smooth layout displacement animation.
  *(Reference: `ConversationalRoutineModification.feature`)*

### [Skill 3: Proactive Behavioral Suggestion]
- **Trigger:** Tapping the "Simulate Critical Friction" action button in the Debug Dashboard.
- **Simulation Flow:**
  1. Mutate the target "Reading Block" entry state to `status = "FRICTION"`.
  2. Invoke the device `Vibrator` subsystem to emit a disruptive, non-habituated haptic wave pattern: `longArrayOf(0, 250, 200, 250, 150, 400)`.
  3. Dispatch a real native Android push notification displaying a high-priority heads-up banner containing the exact empathetic re-scheduling text and an interactive one-tap action action button labeled *"Move to 6:30 PM"*.
  4. Upon intercepting the tap, the system clears the tray and instantaneously routes the reading block timestamp to 18:30 with state set to "REALLOCATED", reflecting immediately on the screen list.

### [Skill 4: External MCP Calendar Reconciliation]
- **Trigger:** Tapping the "Simulate MCP Collision" action button in the Debug Dashboard.
- **Simulation Flow:**
  1. Ingest a mock calendar payload containing an external immutable event: *"Sistemas Operativos Exam"* at 18:00.
  2. Execute the priority parsing rules: Detect a collision with the existing "Gym / Workout" block. Lock the 18:00 slot with a high-priority "External Immutable" themed card layout, and isolate the workout into a `PENDING_REALLOCATION` state.
  3. Immediately push a native notification presenting an empathetic rescheduling proposal to move the workout to 7:30 PM.
  4. Tapping the confirmation option updates the local list cache, re-inserting the workout cleanly at 19:30.

---

## 🚫 STRICT PROGRAMMATIC BOUNDARIES
- **Zero Inline Composables Logic:** No business logic, coroutine latency timers, or native hardware triggers should reside inside an asynchronous `@Composable` function. The UI must view the application state exclusively through the ViewModel's exposed streams.
- **Prohibited Dependencies:** You are explicitly forbidden from compiling third-party libraries such as Retrofit, Ktor, Room, Supabase SDK, or direct AI model integrations. Resolve every operational flow using native Android platform services (`NotificationManager`, `TextToSpeech`, `Vibrator`) and simple raw arrays in memory.
- Ensure all generated code is highly readable, modular, incorporates self-descriptive naming conventions, and compiles immediately without error.

---

## 📋 PRE-EXECUTION CHECKLIST FOR AGENT
- [ ] Read `Architecture/Tech-Stack_Context.md` to ensure correct Compose BOM and native SDK API boundaries are respected.
- [ ] Read `Architecture/General_Project_Context.md` to verify alignment with the product vision.
- [ ] Read `SDD/specifications/SPEC01.md` to perfectly model the Deployment Onboarding state machine.
- [ ] Review the existing codebase and implement the `ui`, `data`, `viewmodel`, and `receiver` packages sequentially as described above.
- [ ] Ensure the MVP is 100% self-contained in-memory before completing the task.
