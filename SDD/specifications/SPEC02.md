# SPEC02: Conversational Routine Modification via Voice

## 1. Traceability

| Attribute             | Value                                                                                                   |
| :-------------------- | :------------------------------------------------------------------------------------------------------ |
| **Source Feature**    | `features/ConversationalRoutineModification.feature`                                                    |
| **Architectural Ref** | `Architectural_Context.md §3.1` — Flow A: Voice-Driven Schedule Shift                                  |
| **Data Ref**          | `Mock_Data_Seed_Context.md §1, §2` — Seed data & voice command payload                                 |
| **UI Ref**            | `UI_Guide_Context.md §2, §4` — Color palette, FAB and card design rules                                |
| **API Ref**           | `Native_API_Recipes_Context.md §1` — Lifecycle-aware TTS engine recipe                                 |
| **Tech Ref**          | `Tech-Stack_Context.md §3, §4, §5` — ViewModel, Coroutines, TTS, prohibited libraries                  |
| **Depends On**        | **SPEC01** (Onboarding completed, user on main dashboard; canonical `HabitBlock` model defined)         |
| **Priority**          | **P1 — Core Feature**                                                                                   |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                       |

---

## 2. Objective

Specify the system's ability to receive a natural language voice command (simulated) that triggers the automatic reorganization of afternoon habit blocks. This flow demonstrates the product's central transformation: **from manual forms to frictionless conversational interaction** (`General_Project_Context.md §3.1`).

---

## 3. Preconditions

| #  | Precondition                                                                                      |
| :- | :------------------------------------------------------------------------------------------------ |
| 1  | The user has completed onboarding (SPEC01) and is on the main dashboard screen.                   |
| 2  | The ViewModel's in-memory collection (`MutableList<HabitBlock>`) is initialized with the afternoon seed data defined in §7. |
| 3  | The Android `TextToSpeech` engine has been correctly initialized in the ViewModel per `Native_API_Recipes_Context.md §1`. |

---

## 4. MVP Simulation Strategy

| Production Component             | MVP Simulation                                                                                   |
| :------------------------------- | :----------------------------------------------------------------------------------------------- |
| Cloud STT (OpenAI Whisper API)   | Simulated: upon releasing the microphone FAB, a hardcoded string is injected as user input.      |
| Cloud NLP (Claude 3.5 Haiku)     | Simulated: hardcoded semantic parser block detecting the input token and triggering transitions.  |
| Network Latency (~1.8s)          | Simulated: `delay(1500)` inside a Kotlin Coroutine (`Dispatchers.Default`) per `Architectural_Context.md §2`. |
| Cloud TTS Response               | **Real**: local `android.speech.tts.TextToSpeech` synthesizes the empathetic response on-device. |
| Room Database Update             | Simulated: atomic mutation of `MutableList<HabitBlock>` in the ViewModel.                        |

---

## 5. Data Model

> **Canonical definition (authoritative source: `Mock_Data_Seed_Context.md §1` and SPEC01 §5).**
> Do **not** re-declare `HabitBlock` here. The model below is reproduced for reference only.

```kotlin
// Canonical model — defined in data/model/HabitBlock.kt (see SPEC01 §5)
data class HabitBlock(
    val id: Int,
    val name: String,
    val scheduledTime: String,   // "HH:mm" — e.g., "17:00"
    val durationMinutes: Int,
    var status: String,          // Canonical literals: "PENDING" | "COMPLETED" | "FRICTION" | "REALLOCATED" | "PENDING_REALLOCATION"
    var isImmutable: Boolean = false,
    var source: String = "INTERNAL"
)
```

**UI State Machine** — managed exclusively inside `RoutInViewModel`:

```kotlin
// file: viewmodel/UiState.kt  (shared across SPEC02, SPEC03, SPEC04)
sealed class UiState {
    object Idle : UiState()
    object Listening : UiState()    // Microphone FAB held: wave animation active
    object Loading : UiState()      // FAB released: simulating cloud inference latency
    object Speaking : UiState()     // TTS engine is vocalizing the response
    data class Success(val message: String) : UiState()
    data class Error(val error: String) : UiState()
}
```

---

## 6. State Machine Specification

Direct reference: `Architectural_Context.md §3.1 — Flow A`.

```
[MainDashboardScreen — uiState = Idle]
    │
    ├─ User PRESS-AND-HOLD MicrophoneFAB
    │     └─► viewModel.onMicPressStart()
    │         └─► uiState = UiState.Listening
    │               └─► UI renders WaveAnimation (Compose Canvas)
    │
    └─ User RELEASES MicrophoneFAB
          │
          ├─► viewModel.onMicRelease()
          │
          ├─► Inject mock STT token (hardcoded string):
          │     "Tengo una junta con mi asesor de Proyecto de Investigación
          │      a las 5, mueve mis hábitos de la tarde"
          │
          ├─► uiState = UiState.Loading
          │     └─► UI continues WaveAnimation
          │
          ├─► viewModelScope.launch(Dispatchers.Default) {
          │       delay(1500)                         // Simulates cloud API inference latency
          │       withContext(Dispatchers.Main) {
          │
          │         ├─► uiState = UiState.Speaking
          │         │     └─► ttsEngine.speak(
          │         │           "Entendido, Gabriel. He protegido tu espacio para la
          │         │            junta de Proyecto de Investigación. Moviendo tus
          │         │            hábitos de la tarde para reducir tu estrés.",
          │         │           TextToSpeech.QUEUE_FLUSH, null, "VoiceTriggerID"
          │         │         )
          │         │
          │         ├─► MUTATE DATA STATE:
          │         │     // 1. Shift all non-immutable blocks scheduled at or after 17:00
          │         │     habitBlocks.forEach { block ->
          │         │         if (block.scheduledTime >= "17:00" && !block.isImmutable) {
          │         │             block.scheduledTime = addMinutes(block.scheduledTime, 60)
          │         │         }
          │         │     }
          │         │     // 2. Insert new immutable meeting block at 17:00
          │         │     habitBlocks.add(insertedMeeting) // See Mock_Data_Seed_Context.md §2
          │         │     habitBlocks.sortBy { it.scheduledTime }
          │         │
          │         └─► uiState = UiState.Success("Schedule updated.")
          │               └─► LazyColumn re-renders with animateItemPlacement()
          │       }
          │   }
          │
          └─► [MainDashboardScreen — uiState = Success, list updated]
```

**Inserted meeting block** (exact values from `Mock_Data_Seed_Context.md §2`):

```kotlin
val insertedMeeting = HabitBlock(
    id = 201,
    name = "Junta de Proyecto de Investigación",
    scheduledTime = "17:00",
    durationMinutes = 60,
    status = "PENDING",
    isImmutable = true,
    source = "INTERNAL"
)
```

---

## 7. Seed Data

> Per `Mock_Data_Seed_Context.md §1`, the canonical baseline seed consists of 3 blocks. For this feature's demonstrable scenario the ViewModel must be initialized with the following **expanded afternoon schedule**, which supersedes the baseline seed for this flow:

| ID  | Block Name           | Scheduled Time | Duration | Status      | isImmutable | source     |
| :-- | :------------------- | :------------- | :------- | :---------- | :---------- | :--------- |
| 101 | Clase de Arquitectura| 14:00          | 120 min  | `COMPLETED` | true        | `EXTERNAL` |
| 102 | Almuerzo             | 14:00          | 60 min   | `PENDING`   | false       | `INTERNAL` |
| 103 | Estudio Cálculo      | 15:00          | 60 min   | `PENDING`   | false       | `INTERNAL` |
| 104 | Proyecto de Software | 16:00          | 60 min   | `PENDING`   | false       | `INTERNAL` |
| 105 | Lectura Recreativa   | 17:00          | 60 min   | `PENDING`   | false       | `INTERNAL` |
| 106 | Gym / Workout        | 18:00          | 90 min   | `PENDING`   | false       | `INTERNAL` |
| 107 | Cena                 | 19:30          | 60 min   | `PENDING`   | false       | `INTERNAL` |

> After executing the voice command, all non-immutable blocks with `scheduledTime >= "17:00"` are shifted +60 minutes to accommodate the new `"Junta de Proyecto de Investigación"` block at 17:00.

**Expected post-mutation state:**

| ID  | Block Name                         | Scheduled Time | Status    |
| :-- | :--------------------------------- | :------------- | :-------- |
| 201 | Junta de Proyecto de Investigación | 17:00          | `PENDING` |
| 105 | Lectura Recreativa                 | 18:00          | `PENDING` |
| 106 | Gym / Workout                      | 19:00          | `PENDING` |
| 107 | Cena                               | 20:30          | `PENDING` |

---

## 8. UI Components Required

Design tokens from `UI_Guide_Context.md §2, §4` apply to all components.

| Component ID     | Type                    | Unique ID (for testing) | Description                                                                                 |
| :--------------- | :---------------------- | :---------------------- | :------------------------------------------------------------------------------------------ |
| `MainDashboard`  | `@Composable` Screen    | `screen_main_dashboard` | Root screen; hosts `LazyColumn` of `HabitBlockCard` items and `MicrophoneFAB`.              |
| `MicrophoneFAB`  | `@Composable` FAB       | `fab_microphone`        | Circular FAB (`VibrantGreenEmphasis`); press-and-hold → `Listening`, release → `Loading`.  |
| `WaveAnimation`  | `@Composable` Canvas    | `anim_wave`             | Native Canvas wave animation, active during `Listening` and `Loading` UI states.            |
| `HabitBlockCard` | `@Composable` Card      | `card_habit_{id}`       | Material 3 card (`RoundedCornerShape(24.dp)`); displays name, time, and status chip.        |
| `DebugDashboard` | `@Composable` Section   | `section_debug`         | Collapsible section at the bottom of `MainDashboard` with manual simulation buttons.        |

---

## 9. Technical Constraints

1. **STT Simulation**: No real `SpeechRecognizer` is used. The input is a literal string constant injected upon FAB release. See `Tech-Stack_Context.md §5`.
2. **Real TTS**: The native `android.speech.tts.TextToSpeech` SDK is used. Initialize with `Locale(\"es\", \"MX\")` (not `Locale.getDefault()`) to match the Spanish-language payload. Invoked from ViewModel exclusively. See `Native_API_Recipes_Context.md §1`.
3. **TTS Locale Override**: The skill file directs `Locale(\"es\", \"MX\")` for the empathetic Spanish response. The `Native_API_Recipes_Context.md §1` recipe uses `Locale.getDefault()`; override it with `Locale(\"es\", \"MX\")` explicitly for this MVP.
4. **TTS Lifecycle**: The TTS engine must be released in `RoutInViewModel.onCleared()` via `ttsEngine?.stop(); ttsEngine?.shutdown()`. See `Architectural_Context.md §4.2` and `Native_API_Recipes_Context.md §1`.
5. **State Isolation**: The mock STT injection, `delay(1500)`, TTS invocation, and list mutation reside **exclusively** in the ViewModel. `@Composable` functions observe `StateFlow<UiState>` and `StateFlow<List<HabitBlock>>` only. See `Architectural_Context.md §4.1`.
6. **Coroutine Dispatcher**: Use `viewModelScope.launch(Dispatchers.Default)` for the delay, then `withContext(Dispatchers.Main)` for TTS and list mutation to ensure thread safety.
7. **Animation**: Block displacement must use `animateItemPlacement()` modifier inside the `LazyColumn`'s item key lambda for smooth visual displacement.
8. **Prohibited Libraries**: No `Retrofit`, `Ktor`, `Room`, `Supabase SDK`, or any AI/network client. See `Tech-Stack_Context.md §5`.

---

## 10. Acceptance Criteria

| #  | Criterion                                                                                              | Verification         |
| :- | :----------------------------------------------------------------------------------------------------- | :------------------- |
| 1  | Pressing and holding the FAB transitions to `UiState.Listening` and displays the wave animation.       | UI Test              |
| 2  | Releasing the FAB injects the mock STT token and transitions to `UiState.Loading`.                     | Unit Test            |
| 3  | After `delay(1500)`, the TTS engine speaks the empathetic confirmation phrase aloud in Spanish.         | Manual / Device Test |
| 4  | All non-immutable blocks with `scheduledTime >= "17:00"` are shifted by +60 minutes.                   | Unit Test            |
| 5  | A new `"Junta de Proyecto de Investigación"` block is inserted at `scheduledTime = "17:00"`.           | Unit Test            |
| 6  | The UI animates card displacement using `animateItemPlacement()` in the `LazyColumn`.                  | UI Test              |
| 7  | No network calls are made and no HTTP clients are imported.                                             | Build Verification   |
| 8  | The TTS engine is correctly released in `onCleared()`.                                                  | Unit Test            |
| 9  | All cards use `RoundedCornerShape(24.dp)` and `RoutInColors` palette tokens.                           | UI Test / Code Review|
