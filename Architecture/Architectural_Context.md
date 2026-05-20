# Rout-In MVP: Architectural Context & State Machine Specification

This document provides the definitive architectural context, structural mapping, and reactive state machine logic for the **Rout-In** MVP. It acts as the context prompt for the agéntic IDE (**Antigravity**), defining how to construct the application's internal structure using an **Offline-First, In-Memory MVVM** pattern to simulate production behaviors.

---

## 1. Architectural Pattern Overview

The presentation layer of the Rout-In MVP strictly matches the production blueprint by employing **Model-View-ViewModel (MVVM)** powered by **Jetpack Compose**. However, to bypass network instabilities during live evaluation, the infrastructure collapses all data persistence, protocol negotiation, and cloud inference layers into a unified, reactive **In-Memory State Machine** located inside the `ViewModel`.

```
       +--------------------------------------------------------+
       |                     VIEW LAYER                         |
       |               (Jetpack Compose Screens)                |
       +--------------------------+-----------------------------+
                                  | Observes Reactive States
         Dispatches User Events   | (StateFlow / MutableState)
         (Clicks, Voice Triggers) |
                                  v
       +--------------------------------------------------------+
       |                   VIEWMODEL LAYER                      |
       |          (RoutInViewModel : State Machine)             |
       |                                                        |
       |  +------------------+           +-------------------+  |
       |  | In-Memory Data   |---------->| Coroutine Timers  |  |
       |  | Collections      |           | & Latency Mocks   |  |
       |  +------------------+           +-------------------+  |
       +--------------------------+-----------------------------+
                                  | Local SDK Interoperability
                                  v
       +--------------------------------------------------------+
       |                 HARDWARE API SIMULATORS                |
       |  [TextToSpeech]   [NotificationManager]   [Vibrator]   |
       +--------------------------------------------------------+
```

---

## 2. Production vs. MVP Architecture Mapping

The agent must translate production dependencies into lightweight local equivalents while maintaining exact behavioral equivalence:

| Architectural Component | Production Design Blueprint | MVP Simulation Strategy (This Architecture) |
| :--- | :--- | :--- |
| **Persistence Layer** | `Room Database` + SQLite indexing. | In-Memory generic Collections (`MutableList<HabitBlock>`) initialized with dummy seed data directly in the `ViewModel`. |
| **Speech-to-Text (STT)** | Cloud-based `OpenAI Whisper API`. | Simulated via user action hold-and-release, treating a pre-baked string as the immediate input upon release. |
| **Natural Language Processing** | Cloud-based `Claude 3.5 Haiku API`. | Hardcoded semantic parser block that processes the input string and triggers corresponding state transitions. |
| **System Latency Representation** | Network roundtrips (< 1.8 seconds). | Explicit non-blocking execution using **`delay(1500)`** within a Kotlin Coroutine block. |
| **Contextual Synchronization (MCP)** | Bidirectional `Model Context Protocol` client. | Simulated via an on-screen **"Debug Dashboard"** section containing manual event triggers. |
| **Cloud Synchronization** | `Supabase WebSockets` (Realtime). | Swapped for local state manipulation; synchronization is considered successful instantly upon state change. |

---

## 3. Core Architectural Drivers (ADD) & State Transitions

The agent must configure the `ViewModel` to react smoothly to the following three state transition sequences, which embody the core architectural drivers of the platform:

### 3.1 Flow A: Voice-Driven Schedule Shift
* **Trigger Event:** User holds and releases the microphone widget, submitting the mock token: `"Tengo una junta con mi asesor de Proyecto de Investigación a las 5, mueve mis hábitos de la tarde"`.
* **State Machine Traversal:**
  1. Set `uiState = UiState.Loading` -> Compose renders the native wave animation.
  2. Suspend execution for `1500ms` using `Dispatchers.Default`.
  3. Execute Text-to-Speech Engine -> Trigger `android.speech.tts.TextToSpeech` to vocalize the empathetic phrase aloud.
  4. Mutate Data State -> Iterate over the list of `HabitBlock` instances; any item with an hour specification $\ge 	ext{5:00 PM}$ has its `scheduledTime` incremented dynamically.
  5. Set `uiState = UiState.Success` -> Jetpack Compose cleanly animates the item displacement.

### 3.2 Flow B: Proactive Habit Re-scheduling (Anti-Blindness Alert)
* **Trigger Event:** User clicks the "Simulate Critical Friction" button inside the Debug Dashboard.
* **State Machine Traversal:**
  1. Mutate a specific target habit (e.g., *Reading Block*) to `status = HabitStatus.FRICTION`.
  2. Build an active Android `NotificationChannel` with high-importance banner attributes.
  3. Attach a custom haptic pattern array to the `Vibrator` instance: `long[] pattern = {0, 250, 200, 250, 150, 400};` to ensure distinct tactile sensation.
  4. Fire a real native Push Notification via `NotificationManager` containing an interactive `PendingIntent` button labeled *"Move to 6:30 PM"*.
  5. **Resolution:** Clicking the notification button bypasses the full app lifecycle, intercepts the `ViewModel` data list, re-routes the reading block to 6:30 PM, and clears the notification tray.

### 3.3 Flow C: External MCP Event Ingestion & Reconciliation
* **Trigger Event:** User clicks the "Simulate MCP Collision" button inside the Debug Dashboard.
* **State Machine Traversal:**
  1. The `ViewModel` ingests a mock external event block: `{ name: "Sistemas Operativos Exam", time: "6:00 PM", immutable: true }`.
  2. The system executes the **Calendar Priority Rule**: It scans the collection for collisions. It finds that *"Gym / Workout"* is scheduled at 6:00 PM.
  3. The state machine locks the 6:00 PM slot for the Exam block, changes its visual styling to an "External Immutable" theme card, and detaches the Gimnasio block, putting it into a `PENDING_REALLOCATION` state.
  4. Trigger a contextual push notification presenting an empathetic rescheduling proposal to move the workout to 7:30 PM.
  5. Upon confirmation, the workout is re-inserted at 7:30 PM, restoring system state parity.

---

## 4. Technical Constraints for Code Generation

When compiling and synthesizing the code structure, the agéntic compiler must respect these programmatic boundaries:

1. **State Isolation:** All UI layers must be strictly declarative. No business or simulation logic should reside within `@Composable` functions. They must view the world exclusively through the `ViewModel`'s state streams.
2. **Resource Management:** Ensure that the `TextToSpeech` engine is correctly initialized with the device's locale (`Locale.getDefault()`) and safely released inside the `onCleared()` lifecycle method of the `ViewModel` to prevent memory leaks on the evaluation device.
3. **No External Libraries:** Any implementation that attempts to reference external JSON parsers, real server endpoints, or remote database plugins must be discarded. Stick to native Kotlin standard library constructs.