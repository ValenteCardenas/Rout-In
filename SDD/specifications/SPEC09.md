# SPEC09: Conversational AI Interface Bottom Sheet

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Architectural Ref** | `Architectural_Context.md §3.2` — Flow B: Conversational Routine Modification                                            |
| **UI Ref**            | `UI_Guide_Context.md`                                                                                                    |
| **Depends On**        | **SPEC02** (Voice Simulation), **SPEC08** (Coaching Message)                                                             |
| **Priority**          | **P2 — Presentation Impact**                                                                                             |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Replace the single hardcoded voice simulation with an interactive **Conversational AI Bottom Sheet** that allows the jury (and users) to type or "speak" natural-language schedule commands. A local keyword-matching engine inside the ViewModel parses the input and executes the appropriate schedule mutation, making the app feel like a real AI assistant rather than a scripted demo.

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | The user is on the Main Dashboard.                                                                              |
| 2  | The ViewModel's in-memory list contains habit blocks.                                                           |

---

## 4. UI Component: Conversational Bottom Sheet

### 4.1 Trigger

- **Microphone FAB**: Instead of directly invoking `onMicPressStart()` / `onMicRelease()`, tapping the Microphone FAB opens a `ModalBottomSheet`.
- The existing press-and-hold voice gesture can optionally remain as a secondary interaction inside the sheet.

### 4.2 Visual Layout

```
┌─────────────────────────────────────────────────────┐
│           ── (drag handle) ──                       │
│                                                     │
│     🎙  Hablemos con Rout-In                        │
│     Escribe o dicta un comando                      │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  "mueve mi gimnasio a las 8"              🔍  │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Sugerencias rápidas:                               │
│  ┌──────────────────┐ ┌──────────────────────────┐  │
│  │ 📅 Mover hábito  │ │ ✅ Completar siguiente   │  │
│  └──────────────────┘ └──────────────────────────┘  │
│  ┌──────────────────┐ ┌──────────────────────────┐  │
│  │ 🆕 Agregar rutina│ │ 🗑 Cancelar siguiente    │  │
│  └──────────────────┘ └──────────────────────────┘  │
│                                                     │
│  ── (wave animation here while processing) ──       │
│                                                     │
│  💬 "Entendido, Gabriel. Moviendo tu gimnasio       │
│      a las 20:00."                                  │
└─────────────────────────────────────────────────────┘
```

- **Container**: `ModalBottomSheet` with `containerColor = RoutInColors.DarkSurface`, `shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`.
- **Input Field**: `OutlinedTextField` styled with the established design tokens. A trailing send/search icon triggers processing.
- **Quick Suggestion Chips**: A `FlowRow` (or two `Row`s) of tappable `SuggestionChip` or styled small `Card` elements. Tapping one pre-fills the text field with the corresponding command.
- **Response Area**: A text block that shows the AI's parsed response after processing. Animated with `AnimatedVisibility(fadeIn)`.

### 4.3 Quick Suggestion Chips

| Chip Label             | Pre-filled Command                                                   |
| :--------------------- | :------------------------------------------------------------------- |
| 📅 Mover hábito        | `"mueve mi gimnasio a las 20:00"`                                    |
| ✅ Completar siguiente  | `"ya terminé mi siguiente hábito"`                                   |
| 🆕 Agregar rutina      | `"agrega estudio de cálculo a las 21:00 por 45 minutos"`            |
| 🗑 Cancelar siguiente  | `"cancela mi siguiente hábito"`                                      |

---

## 5. ViewModel: Local NLP Keyword Engine

### 5.1 Strategy

A deterministic, regex-based command parser inside the ViewModel. No actual NLP — just robust keyword matching with Spanish-language patterns. The engine must:

1. Accept a raw input string.
2. Match it against a priority-ordered list of command patterns.
3. Execute the corresponding schedule mutation.
4. Return a response string for the UI to display and for TTS to vocalize.

### 5.2 Command Pattern Table

| Pattern Keywords                                            | Action                                                        | TTS Response Template                                                           |
| :---------------------------------------------------------- | :------------------------------------------------------------ | :------------------------------------------------------------------------------ |
| `"mueve"` + habit name match + `"a las {HH:mm}"`           | Find habit by fuzzy name match, update `scheduledTime`        | `"Entendido, Gabriel. Moviendo {name} a las {time}."` |
| `"terminé"` / `"completé"` / `"listo"`                      | Toggle next PENDING habit to COMPLETED                        | `"¡Excelente! Marcando {name} como completado."` |
| `"agrega"` / `"nueva"` + name + `"a las {HH:mm}"`         | Create new habit via `addCustomHabit`                         | `"Perfecto. Agregando {name} a las {time} a tu agenda."` |
| `"cancela"` / `"elimina"` + habit name match                | Delete matching habit via `deleteCustomHabit`                 | `"Entendido. Eliminando {name} de tu agenda."` |
| `"junta"` / `"reunión"` + `"a las {HH:mm}"`               | Execute the SPEC02 voice flow (inject meeting, shift habits)  | `"He protegido tu espacio para la junta. Moviendo tus hábitos de la tarde."` |
| No match                                                    | No mutation                                                   | `"Lo siento, no entendí ese comando. Intenta con 'mueve mi gimnasio a las 8'."` |

### 5.3 Implementation Contract

```kotlin
data class CommandResult(
    val responseText: String,
    val wasUnderstood: Boolean
)

fun processNaturalCommand(input: String): CommandResult {
    val normalized = input.lowercase().trim()

    // Pattern 1: Move habit
    val movePattern = Regex("""mueve?\s+(?:mi\s+)?(.+?)\s+a\s+las?\s+(\d{1,2}(?::\d{2})?)""")
    movePattern.find(normalized)?.let { match ->
        val habitQuery = match.groupValues[1]
        val rawTime = match.groupValues[2]
        val time = normalizeTime(rawTime) // "8" -> "08:00", "20:00" -> "20:00"
        val habit = fuzzyFindHabit(habitQuery)
        if (habit != null) {
            updateCustomHabit(habit.id, habit.name, time, habit.durationMinutes, habit.isImmutable)
            return CommandResult(
                "Entendido, Gabriel. Moviendo ${habit.name} a las $time.",
                true
            )
        }
    }

    // Pattern 2: Complete next pending
    // ...

    return CommandResult("Lo siento, no entendí ese comando.", false)
}
```

### 5.4 Fuzzy Habit Name Matching

```kotlin
private fun fuzzyFindHabit(query: String): HabitBlock? {
    val normalized = query.lowercase().trim()
    return _habitBlocks.value.find { block ->
        val name = block.name.lowercase()
        name.contains(normalized) || normalized.contains(name.split(" ").first())
    }
}
```

### 5.5 Time Normalization

```kotlin
private fun normalizeTime(raw: String): String {
    return if (raw.contains(":")) {
        val parts = raw.split(":")
        "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    } else {
        "%02d:00".format(raw.toInt())
    }
}
```

---

## 6. Simulation Flow

```
[User taps Microphone FAB]
    │
    ├─► ModalBottomSheet slides up
    │
    ├─ User types command or taps quick suggestion chip
    │   │
    │   ├─► Set UI state to Loading
    │   ├─► Show wave animation in sheet
    │   ├─► delay(1200L) — simulate processing latency
    │   │
    │   ├─► viewModel.processNaturalCommand(input)
    │   │
    │   ├─► Display response text in the sheet
    │   ├─► TextToSpeech vocalizes the response
    │   │
    │   ├─► delay(2000L) — let user read the response
    │   └─► Sheet auto-dismisses (or stays open for another command)
```

---

## 7. Technical Constraints

1. **No External NLP**: The keyword engine is purely local regex matching. No network calls.
2. **TTS Reuse**: Reuse the existing `speakEmpatheticResponse()` method from SPEC02 — do not create a second TTS engine instance.
3. **Bottom Sheet Compose**: Use `ModalBottomSheet` from `androidx.compose.material3`. Requires `@OptIn(ExperimentalMaterial3Api::class)`.
4. **State Isolation**: The bottom sheet only gathers input and displays output. All mutations happen in the ViewModel.
5. **Graceful Fallback**: Unrecognized commands must display a helpful, non-judgmental response suggesting valid command formats.
6. **Existing FAB Behavior**: The press-and-hold mic interaction from SPEC02 can be preserved as an alternative trigger inside the bottom sheet, but the FAB's primary action changes from press-hold to single-tap-open-sheet.

---

## 8. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Tapping the Microphone FAB opens the Conversational Bottom Sheet.                                          | UI Test          |
| 2  | Typing "mueve mi gimnasio a las 20:00" correctly updates the Gym block's time.                             | UI / Unit Test   |
| 3  | Typing "ya terminé" marks the next pending habit as COMPLETED.                                             | UI / Unit Test   |
| 4  | Tapping a Quick Suggestion Chip pre-fills the input field with the corresponding command.                  | UI Test          |
| 5  | The TTS engine vocalizes the response aloud after processing.                                              | Manual Test      |
| 6  | Unrecognized input displays a helpful fallback message without crashing.                                   | Unit Test        |
