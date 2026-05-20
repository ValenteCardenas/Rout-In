# Rout-In MVP: Mock Data Seed & Simulation Payloads

This document defines the static datasets, string constants, and hardcoded JSON payloads used to feed the in-memory collections within the `ViewModel`. The agéntic IDE (**Antigravity**) must instantiate these structures exactly as written to build the data-driven narrative required for the presentation.

---

## 1. Initial State Data Base Seed (In-Memory Habit Blocks)

This Kotlin structure represents the default state of Gabriel's afternoon schedule inside the in-memory array list before any user voice interaction or external network collisions occur.

### Implementation Snippet:
```kotlin
data class HabitBlock(
    val id: Int,
    val name: String,
    val scheduledTime: String, // Stored as HH:mm for simple parsing
    val durationMinutes: Int,
    var status: String,        // "PENDING", "COMPLETED", "FRICTION", "REALLOCATED"
    var isImmutable: Boolean = false,
    var source: String = "INTERNAL" // "INTERNAL" (Rout-In) or "EXTERNAL" (MCP / Google Calendar)
)

object MockDataSeed {
    fun getInitialHabits(): MutableList<HabitBlock> {
        return mutableListOf(
            HabitBlock(
                id = 101,
                name = "Clase de Arquitectura",
                scheduledTime = "14:00",
                durationMinutes = 120,
                status = "COMPLETED",
                isImmutable = true,
                source = "EXTERNAL"
            ),
            HabitBlock(
                id = 102,
                name = "Reading Block",
                scheduledTime = "16:00",
                durationMinutes = 60,
                status = "PENDING",
                isImmutable = false,
                source = "INTERNAL"
            ),
            HabitBlock(
                id = 103,
                name = "Gym / Workout",
                scheduledTime = "18:00",
                durationMinutes = 90,
                status = "PENDING",
                isImmutable = false,
                source = "INTERNAL"
            )
        )
    }
}

```

---

## 2. Feature 2: Voice Command Trigger & Empathetic TTS Payload

These string constants are mapped directly to the speech-to-text simulation trigger event. The output string must be passed to the native Text-to-Speech hardware execution channel.

* **Expected Speech Input Match:** `"Tengo una junta con mi asesor de Proyecto de Investigación a las 5, mueve mis hábitos de la tarde"`
* **Empathetic Text-to-Speech (TTS) Output:**
`"Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."`

### Resulting Target State Modification Matrix:

```kotlin
// Execution mapping for the simulated state transformation:
val insertedMeeting = HabitBlock(
    id = 201,
    name = "Junta de Proyecto de Investigación",
    scheduledTime = "17:00",
    durationMinutes = 60,
    status = "PENDING",
    isImmutable = true,
    source = "INTERNAL"
)

// Structural Shift: All internal, non-immutable habits scheduled >= 17:00 
// shift forward automatically by 60 minutes.
// "Gym / Workout" (18:00) transitions dynamically to scheduledTime = "19:00"

```

---

## 3. Feature 3: Behavioral Suggestion Context Strings

When the debug trigger forces a "Critical Friction" scenario on a routine block, the notification sub-system parameters must be populated using these literal definitions.

* **Target Habit ID to Intercept:** `102` ("Reading Block")
* **New In-Memory Mock Status:** `"FRICTION"`
* **Notification Content Text:** `"Hola. Notamos que este horario te ha costado trabajo últimamente. No te preocupes, vamos a tu ritmo. ¿Prefieres mover la lectura a hoy a las 6:30 PM o prefieres que lo intentemos el sábado por la mañana?"`
* **One-Tap Action Payload Configuration:**
* Action Button 1 Label: `"Move to 6:30 PM"` -> Triggers state change: `scheduledTime = "18:30"`, `status = "REALLOCATED"`.
* Action Button 2 Label: `"Cancel"` -> Dismisses view container with no state impact.



---

## 4. Feature 4: External MCP Event Collision Payload

This mock JSON schema models the payload injected asynchronously through the local broadcast or event pipeline to simulate a real-time Server-Sent Event (SSE) or WebSocket push notification from an external calendar channel.

### Simulated Inbound MCP Payload Object:

```json
{
  "mcp_event_id": "mcp_evt_os_992",
  "event_source": "Google Calendar Server",
  "event_name": "Sistemas Operativos Exam",
  "start_time": "18:00",
  "duration_minutes": 120,
  "is_immutable": true,
  "empathetic_alert_message": "Tu examen de Sistemas Operativos se movió a las 6:00 PM e interfiere con tu gimnasio. Hemos protegido tu bloque académico para que te enfoques. ¿Deseas reubicar tu rutina de entrenamiento hoy a las 7:30 PM o prefieres que la IA recalcule tu semana?"
}

```

### Resulting Target State Modification Matrix:

```kotlin
// Upon receiving the mock object above, the state engine forces:
// 1. Insert "Sistemas Operativos Exam" block directly into the UI list at 18:00 (Immutable theme)
// 2. Displace "Gym / Workout" (Id: 103) from 18:00 slot -> status sets to "PENDING_REALLOCATION"
// 3. Fire the notification banner with the embedded text payload.
// 4. One-Tap Action "Re-locate to 7:30 PM" re-inserts Gym at "19:30".

```