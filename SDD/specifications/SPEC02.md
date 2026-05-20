# SPEC02: Conversational Routine Modification via Voice

## 1. Traceability

| Attribute             | Value                                                                            |
| :-------------------- | :------------------------------------------------------------------------------- |
| **Source Feature**    | `features/ConversationalRoutineModification.feature`                             |
| **Architectural Ref** | `Architectural_Context.md §3.1` — Flow A: Voice-Driven Schedule Shift           |
| **Depends On**        | **SPEC01** (Onboarding completed, user on main dashboard)                        |
| **Priority**          | **P1 — Core Feature**                                                            |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                |

---

## 2. Objective

Specify the system's ability to receive a natural language voice command (simulated) that triggers the automatic reorganization of afternoon habit blocks. This flow demonstrates the product's central transformation: **from manual forms to frictionless conversational interaction**.

---

## 3. Preconditions

| #  | Precondition                                                                                      |
| :- | :------------------------------------------------------------------------------------------------ |
| 1  | The user has completed onboarding (SPEC01) and is on the main dashboard screen.                   |
| 2  | The ViewModel's in-memory collection (`MutableList<HabitBlock>`) contains afternoon habit blocks with predefined seed data. |
| 3  | The Android `TextToSpeech` engine has been correctly initialized in the ViewModel.                 |

---

## 4. MVP Simulation Strategy

| Production Component             | MVP Simulation                                                                                |
| :------------------------------- | :-------------------------------------------------------------------------------------------- |
| Cloud STT (OpenAI Whisper API)   | Simulated: upon releasing the microphone FAB, a hardcoded string is injected as user input.    |
| Cloud NLP (Claude 3.5 Haiku)     | Simulated: hardcoded semantic parser that detects the input token and triggers state transitions. |
| Network Latency (~1.8s)          | Simulated: `delay(1500)` inside a Kotlin Coroutine (`Dispatchers.Default`).                    |
| Cloud TTS Response               | Real: local `android.speech.tts.TextToSpeech` synthesizes the empathetic response on the device. |
| Room Database Update             | Simulated: atomic mutation of `MutableList<HabitBlock>` in the ViewModel.                      |

---

## 5. Data Model

```kotlin
data class HabitBlock(
    val id: String,
    val name: String,
    var scheduledTime: LocalTime,
    var status: HabitStatus = HabitStatus.ACTIVE,
    val isExternal: Boolean = false,
    val isImmutable: Boolean = false
)

enum class HabitStatus {
    ACTIVE,
    FRICTION,
    PENDING_REALLOCATION,
    DISPLACED
}

sealed class UiState {
    object Idle : UiState()
    object Listening : UiState()   // Microphone active
    object Loading : UiState()     // Processing (wave animation)
    object Speaking : UiState()    // TTS playing response
    data class Success(val message: String) : UiState()
    data class Error(val error: String) : UiState()
}
```

---

## 6. State Machine Specification

Direct reference: `Architectural_Context.md §3.1 — Flow A`.

```
[Dashboard — Idle State]
    │
    ├─ User PRESS-AND-HOLD microphone FAB
    │     └─► uiState = UiState.Listening
    │         └─► UI renders: wave animation (Jetpack Compose Canvas)
    │
    ├─ User RELEASES microphone FAB
    │     │
    │     ├─► Inject mock STT token:
    │     │   "Tengo una junta con mi asesor de Proyecto de Investigación
    │     │    a las 5, mueve mis hábitos de la tarde"
    │     │
    │     ├─► uiState = UiState.Loading
    │     │     └─► UI continues wave animation
    │     │
    │     ├─► viewModelScope.launch(Dispatchers.Default) {
    │     │       delay(1500)  // Simulates cloud API inference latency
    │     │   }
    │     │
    │     ├─► uiState = UiState.Speaking
    │     │     └─► TextToSpeech.speak():
    │     │         "Entendido, Gabriel. He protegido tu espacio para la
    │     │          junta de Proyecto de Investigación. Moviendo tus
    │     │          hábitos de la tarde para reducir tu estrés."
    │     │
    │     ├─► MUTATE DATA STATE:
    │     │     for each block in habitBlocks:
    │     │       if block.scheduledTime >= 17:00:
    │     │         block.scheduledTime += dynamicOffset
    │     │     Insert new block: "Junta Proyecto de Investigación" at 17:00
    │     │
    │     └─► uiState = UiState.Success
    │           └─► UI: Compose animateItemPlacement() on LazyColumn
    │
    └─► [Dashboard — Updated State]
```

---

## 7. Seed Data Required

The ViewModel must be initialized with the following afternoon habit blocks to make the scenario demonstrable:

| Block Name            | Scheduled Time | Status  |
| :-------------------- | :------------- | :------ |
| Almuerzo              | 2:00 PM        | ACTIVE  |
| Estudio Cálculo       | 3:00 PM        | ACTIVE  |
| Proyecto de Software  | 4:00 PM        | ACTIVE  |
| Lectura Recreativa    | 5:00 PM        | ACTIVE  |
| Gym / Workout         | 6:00 PM        | ACTIVE  |
| Cena                  | 7:30 PM        | ACTIVE  |

After executing the voice command, all blocks from 5:00 PM onward must be dynamically displaced to accommodate the new meeting block.

---

## 8. UI Components Required

| Component ID          | Type                    | Description                                                                       |
| :-------------------- | :---------------------- | :-------------------------------------------------------------------------------- |
| `MainDashboard`       | `@Composable` Screen    | Main screen with a `LazyColumn` of `HabitBlockCard` items and a microphone FAB.   |
| `MicrophoneFAB`       | `@Composable` FAB       | Floating Action Button with press-and-hold and release event detection.            |
| `WaveAnimation`       | `@Composable` Canvas    | Native wave animation in Compose, activated during `Listening` and `Loading` states. |
| `HabitBlockCard`      | `@Composable` Card      | Material 3 card displaying habit name, scheduled time, and visual status.          |

---

## 9. Technical Constraints

1. **STT Simulation**: No real `SpeechRecognizer` is used. The input is a literal string injected upon FAB release (`Tech-Stack_Context.md §5`).
2. **Real TTS**: The native `android.speech.tts.TextToSpeech` SDK is used. It must be initialized with `Locale("es", "MX")` and invoked from the ViewModel.
3. **TTS Lifecycle**: The TTS engine must be released in `ViewModel.onCleared()` to prevent memory leaks (`Architectural_Context.md §4.2`).
4. **State Isolation**: Parsing logic, delay, and data mutation reside **exclusively** in the ViewModel. `@Composable` functions only observe `StateFlow` (`Architectural_Context.md §4.1`).
5. **Animation**: Block displacement must use `animateItemPlacement()` from Compose's `LazyColumn` for a smooth visual transition.

---

## 10. Acceptance Criteria

| #  | Criterion                                                                                           | Verification        |
| :- | :-------------------------------------------------------------------------------------------------- | :------------------ |
| 1  | Pressing and holding the FAB displays the wave animation in the UI.                                  | UI Test             |
| 2  | Releasing the FAB injects the mock STT token and transitions to `Loading`.                           | Unit Test           |
| 3  | After `delay(1500)`, the TTS engine speaks the empathetic confirmation phrase aloud.                  | Manual / Device Test|
| 4  | All blocks with time ≥ 5:00 PM are displaced to accommodate the new meeting block.                   | Unit Test           |
| 5  | The UI animates card displacement using `animateItemPlacement()`.                                     | UI Test             |
| 6  | No network calls are made and no HTTP clients are imported.                                           | Build Verification  |
| 7  | The TTS engine is correctly released in `onCleared()`.                                                | Unit Test           |
