# SPEC04: External Calendar Synchronization and Collision Reconciliation

## 1. Traceability

| Attribute | Value |
| :--- | :--- |
| **Source Feature** | `features/ExternalCalendarSyncronization.feature` |
| **Architectural Ref** | `Architectural_Context.md §3.3` — Flow C: External MCP Event Ingestion |
| **Depends On** | **SPEC01** (Premium Cloud mode); **SPEC02** (`HabitBlock` model); **SPEC03** (notification infra) |
| **Priority** | **P2 — Extended Feature** |

---

## 2. Objective

Specify the automatic reconciliation flow when an immutable external event (from Google Calendar via MCP) collides with an internal habit block. The system applies the **Calendar Priority Rule**, protects the academic event, and proposes relocation of the displaced habit. Demonstrates: **from isolated software to open ecosystems (MCP)**.

---

## 3. Preconditions

| # | Precondition |
| :- | :--- |
| 1 | The user is operating under Premium Cloud Subscription mode (SPEC01). |
| 2 | The ViewModel's in-memory collection has a "Gym / Workout" block at 6:00 PM. |
| 3 | The Debug Dashboard is accessible to simulate external MCP events. |

---

## 4. MVP Simulation Strategy

| Production Component | MVP Simulation |
| :--- | :--- |
| MCP Client (Model Context Protocol) | Simulated: "Simulate MCP Collision" button in Debug Dashboard. |
| Google Calendar Server payload | Simulated: hardcoded `ExternalEvent` object injected into ViewModel. |
| Supabase WebSockets sync | Simulated: local state change = instant successful sync. |
| Push Notification orchestration | Real: native `NotificationManager` (reuses SPEC03 infrastructure). |

---

## 5. Data Model Extensions

Reuses models from SPEC02 and SPEC03. Extension for external events:

```kotlin
data class ExternalEvent(
    val name: String,             // e.g., "Sistemas Operativos Exam"
    val scheduledTime: LocalTime, // e.g., 18:00
    val isImmutable: Boolean = true,
    val source: String = "Google Calendar"
)

// HabitStatus.PENDING_REALLOCATION already defined in SPEC02
```

---

## 6. State Machine Specification

Reference: `Architectural_Context.md §3.3 — Flow C`.

```
[Debug Dashboard]
    │
    ├─ User clicks "Simulate MCP Collision"
    │   │
    │   ├─► INGEST EXTERNAL EVENT:
    │   │     ExternalEvent(name="Sistemas Operativos Exam",
    │   │       scheduledTime=18:00, isImmutable=true)
    │   │
    │   ├─► EXECUTE CALENDAR PRIORITY RULE:
    │   │     Scan habitBlocks for collision at 18:00
    │   │     Found: "Gym / Workout" at 18:00
    │   │
    │   ├─► LOCK SLOT FOR EXAM:
    │   │     Insert exam as HabitBlock(isExternal=true, isImmutable=true)
    │   │     Apply "External Immutable" visual theme to card
    │   │
    │   ├─► DISPLACE GYM BLOCK:
    │   │     gymBlock.status = HabitStatus.PENDING_REALLOCATION
    │   │     Detach from 18:00 slot
    │   │
    │   ├─► FIRE CONTEXTUAL NOTIFICATION:
    │   │     "Tu examen de Sistemas Operativos se movió a las 6:00 PM
    │   │      e interfiere con tu gimnasio. Hemos protegido tu bloque
    │   │      académico para que te enfoques. ¿Deseas reubicar tu
    │   │      rutina de entrenamiento hoy a las 7:30 PM o prefieres
    │   │      que la IA recalcule tu semana?"
    │   │     addAction("Re-locate to 7:30 PM", pendingIntent)
    │   │
    │   └─► Waiting for user interaction
    │
    └─ User taps "Re-locate to 7:30 PM"
        ├─► gymBlock.scheduledTime = 19:30
        ├─► gymBlock.status = HabitStatus.ACTIVE
        ├─► NotificationManager.cancel(NOTIFICATION_ID)
        ├─► Sync delta queued (simulated: instant local state update)
        └─► Dashboard reflects: Exam at 18:00, Gym at 19:30
```

---

## 7. Seed Data

Reuses blocks from SPEC02. Key block for this spec:

| Block Name | Time | Status | isExternal | isImmutable |
| :--- | :--- | :--- | :--- | :--- |
| Gym / Workout | 6:00 PM | ACTIVE | false | false |

After MCP simulation:

| Block Name | Time | Status | isExternal | isImmutable |
| :--- | :--- | :--- | :--- | :--- |
| Sistemas Operativos Exam | 6:00 PM | ACTIVE | true | true |
| Gym / Workout | 7:30 PM | ACTIVE (post-confirm) | false | false |

---

## 8. UI Components Required

| Component ID | Type | Description |
| :--- | :--- | :--- |
| `SimulateMCPButton` | `@Composable` Button | Debug Dashboard button that injects the mock MCP event. |
| `ExternalImmutableCard` | `@Composable` Card | Visual variant with "External Immutable" theme (distinct border, icon, color). |
| `PendingReallocationCard` | `@Composable` Card | Visual variant for `PENDING_REALLOCATION` blocks (reduced opacity, clock icon). |
| Native Notification | Android System UI | Contextual notification with relocation proposal. |

---

## 9. Technical Constraints

1. **No Real MCP Client**: MCP communication is simulated via direct object injection into the ViewModel (`Architectural_Context.md §2`).
2. **No WebSockets**: Supabase synchronization is considered instantly successful upon local state mutation (`Tech-Stack_Context.md §5`).
3. **Calendar Priority Rule**: Events with `isImmutable = true` always take precedence over internal blocks when a time collision occurs.
4. **NotificationChannel Reuse**: Uses the same `rout_in_proactive` channel defined in SPEC03.
5. **Visual Differentiation**: External immutable blocks must be visually distinguishable from the user's internal blocks.

---

## 10. Acceptance Criteria

| # | Criterion | Verification |
| :- | :--- | :--- |
| 1 | Pressing "Simulate MCP Collision" inserts the exam block as immutable at 6:00 PM. | Unit Test |
| 2 | The "Gym / Workout" block transitions to `PENDING_REALLOCATION` status. | Unit Test |
| 3 | The exam block is displayed with the differentiated "External Immutable" visual theme. | UI Test |
| 4 | A contextual notification is dispatched with the empathetic text and relocation option. | Device Test |
| 5 | Confirming "Re-locate to 7:30 PM" reschedules the gym and restores `ACTIVE` status. | Unit Test |
| 6 | The notification is dismissed after confirmation. | Device Test |
| 7 | The dashboard shows both blocks (exam at 6:00 PM, gym at 7:30 PM) correctly. | UI Test |
| 8 | No real MCP, WebSocket, or HTTP clients are imported. | Build Verify |
