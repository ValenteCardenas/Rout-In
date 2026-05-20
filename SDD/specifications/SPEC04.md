# SPEC04: External Calendar Synchronization and Collision Reconciliation

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/ExternalCalendarSyncronization.feature`                                                                        |
| **Architectural Ref** | `Architectural_Context.md §3.3` — Flow C: External MCP Event Ingestion & Reconciliation                                 |
| **Data Ref**          | `Mock_Data_Seed_Context.md §4` — MCP collision payload & state modification matrix                                       |
| **UI Ref**            | `UI_Guide_Context.md §2, §4` — Color palette (ClarityBlue for external blocks), component rules                         |
| **API Ref**           | `Native_API_Recipes_Context.md §2, §3` — Notification channel, interactive notification with action buttons              |
| **Tech Ref**          | `Tech-Stack_Context.md §4, §5` — Native SDK APIs, prohibited libraries                                                  |
| **Depends On**        | **SPEC01** (Premium Cloud mode + channel bootstrap); **SPEC02** (`HabitBlock` model); **SPEC03** (`NotificationConfig`)  |
| **Priority**          | **P2 — Extended Feature**                                                                                                |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Specify the automatic reconciliation flow when an immutable external event (from Google Calendar via MCP) collides with an internal habit block. The system applies the **Calendar Priority Rule**, locks the academic event's time slot, and proposes relocation of the displaced habit. Demonstrates: **from isolated software to open ecosystems (MCP)** (`General_Project_Context.md §3.3`).

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | Onboarding has been completed (SPEC01) with `selectedMode = PREMIUM_CLOUD_SUBSCRIPTION`.                         |
| 2  | `MainActivity.onCreate()` has already created the `"rout_in_behavioral_alerts"` `NotificationChannel` (SPEC01). |
| 3  | The ViewModel's in-memory list contains a `"Gym / Workout"` block with `id = 106`. Its `scheduledTime` must be `"18:00"` for the collision to occur — `simulateMcpCollision()` enforces this via precondition reset (see §9.9). |
| 4  | The `DebugDashboard` section is visible and accessible on `MainDashboardScreen`.                                  |

---

## 4. MVP Simulation Strategy

| Production Component                  | MVP Simulation                                                                                   |
| :------------------------------------ | :----------------------------------------------------------------------------------------------- |
| MCP Client (Model Context Protocol)   | Simulated: "Simulate MCP Collision" button in `DebugDashboard`.                                  |
| Google Calendar Server payload        | Simulated: hardcoded `ExternalEvent` object injected into the ViewModel.                         |
| Supabase WebSockets sync              | Simulated: local state change = instant successful sync (no real socket opened).                 |
| Push Notification orchestration       | **Real**: native `NotificationManager` — reuses `NotificationConfig` from SPEC03.               |

---

## 5. Data Model Extensions

> Reuses canonical `HabitBlock` (SPEC01 §5), `UiState` (SPEC02 §5), and `NotificationConfig` (SPEC03 §5). The model below is SPEC04-specific.

```kotlin
// file: data/model/ExternalEvent.kt

data class ExternalEvent(
    val mcpEventId: String,         // e.g., "mcp_evt_os_992"
    val name: String,               // e.g., "Sistemas Operativos Exam"
    val scheduledTime: String,      // "HH:mm" — e.g., "18:00"
    val durationMinutes: Int,       // e.g., 120
    val isImmutable: Boolean = true,
    val source: String = "Google Calendar"
)
```

**Calendar Priority Rule** (programmatic contract):

```kotlin
// Enforced inside RoutInViewModel.ingestExternalEvent()
fun applyCalendarPriorityRule(event: ExternalEvent, habits: MutableList<HabitBlock>) {
    // 1. Find any internal block colliding at event.scheduledTime
    val displaced = habits.find {
        it.scheduledTime == event.scheduledTime && !it.isImmutable
    }
    // 2. Displace the conflicting block
    displaced?.let { it.status = "PENDING_REALLOCATION" }
    // 3. Insert the external event as an immutable HabitBlock
    habits.add(HabitBlock(
        id = event.mcpEventId.hashCode(),
        name = event.name,
        scheduledTime = event.scheduledTime,
        durationMinutes = event.durationMinutes,
        status = "PENDING",
        isImmutable = true,
        source = "EXTERNAL"
    ))
    habits.sortBy { it.scheduledTime }
}
```

**Mock MCP payload** (exact values from `Mock_Data_Seed_Context.md §4`):

```kotlin
val mockMcpEvent = ExternalEvent(
    mcpEventId = "mcp_evt_os_992",
    name = "Sistemas Operativos Exam",
    scheduledTime = "18:00",
    durationMinutes = 120,
    isImmutable = true,
    source = "Google Calendar"
)
```

---

## 6. State Machine Specification

Reference: `Architectural_Context.md §3.3 — Flow C`.

```
[MainDashboardScreen → DebugDashboard section]
    │
    ├─ User clicks "Simulate MCP Collision"
    │   │
    │   ├─► viewModel.simulateMcpCollision()
    │   │
    │   ├─► INGEST EXTERNAL EVENT:
    │   │     val event = mockMcpEvent  // see §5 above
    │   │
    │   ├─► EXECUTE CALENDAR PRIORITY RULE:
    │   │     applyCalendarPriorityRule(event, habitBlocks)
    │   │     // → habitBlocks[id=106] (Gym / Workout at 18:00) → status = "PENDING_REALLOCATION"
    │   │     // → New HabitBlock(Sistemas Operativos Exam, 18:00, isImmutable=true) inserted
    │   │
    │   ├─► UI RENDER UPDATE (reactive via StateFlow):
    │   │     • Exam card renders with ExternalImmutableCard variant (ClarityBlue background, 🔒 icon)
    │   │     • Gym card renders with PendingReallocationCard variant (reduced opacity, 🕐 icon)
    │   │
    │   ├─► DISPATCH CONTEXTUAL NOTIFICATION (real hardware):
    │   │     NotificationManager.notify(
    │   │       NOTIFICATION_ID_MCP,   // = 1002 from NotificationConfig
    │   │       Notification {
    │   │         channel: "rout_in_behavioral_alerts"
    │   │         priority: IMPORTANCE_HIGH
    │   │         title: "Rout-In Assistant"
    │   │         text: "Tu examen de Sistemas Operativos se movió a las 6:00 PM
    │   │                e interfiere con tu gimnasio. Hemos protegido tu bloque
    │   │                académico para que te enfoques. ¿Deseas reubicar tu
    │   │                rutina de entrenamiento hoy a las 7:30 PM o prefieres
    │   │                que la IA recalcule tu semana?"
    │   │         action[0]: Label="Re-locate to 7:30 PM"
    │   │                     PendingIntent → NotificationActionReceiver
    │   │                     (ACTION_RELOCATE_730, EXTRA_HABIT_ID=106, NOTIFICATION_ID=1002)
    │   │       }
    │   │     )
    │   │
    │   └─► [Waiting for user interaction on notification tray]
    │
    └─ User taps "Re-locate to 7:30 PM"
        │
        ├─► NotificationActionReceiver.onReceive(ACTION_RELOCATE_730)
        │
        ├─► NotificationManager.cancel(NOTIFICATION_ID_MCP)
        │
        ├─► context.sendBroadcast(Intent("UPDATE_UI_STATE_GYM_RELOCATE"))
        │   // RoutInViewModel listens via a registered BroadcastReceiver
        │
        ├─► MUTATE DATA STATE:
        │     habitBlocks[id=106].scheduledTime = "19:30"
        │     habitBlocks[id=106].status = "PENDING"
        │     habitBlocks.sortBy { it.scheduledTime }
        │
        └─► [MainDashboardScreen reflects: Exam at 18:00, Gym at 19:30]
```

---

## 7. Seed Data

Reuses the expanded seed from SPEC02 §7. Key block for this spec:

| ID  | Block Name    | Time  | Status    | isExternal | isImmutable |
| :-- | :------------ | :---- | :-------- | :--------- | :---------- |
| 106 | Gym / Workout | 18:00 | `PENDING` | false      | false       |

**After MCP collision simulation (pre-user confirmation):**

| ID        | Block Name               | Time  | Status                   | isExternal | isImmutable |
| :-------- | :----------------------- | :---- | :----------------------- | :--------- | :---------- |
| *(hash)*  | Sistemas Operativos Exam | 18:00 | `PENDING`                | true       | true        |
| 106       | Gym / Workout            | 18:00 | `PENDING_REALLOCATION`   | false      | false       |

**After user confirms "Re-locate to 7:30 PM":**

| ID        | Block Name               | Time  | Status    | isExternal | isImmutable |
| :-------- | :----------------------- | :---- | :-------- | :--------- | :---------- |
| *(hash)*  | Sistemas Operativos Exam | 18:00 | `PENDING` | true       | true        |
| 106       | Gym / Workout            | 19:30 | `PENDING` | false      | false       |

---

## 8. UI Components Required

Design tokens from `UI_Guide_Context.md §2, §4`:
- External Immutable card background: `RoutInColors.ClarityBlue` (with text swapping to `DeepPurpleNavy`)
- Pending Reallocation card: `RoutInColors.DarkSurface` with reduced alpha (0.5f), clock icon overlay
- Card shape: `RoundedCornerShape(24.dp)`

| Component ID               | Type                    | Unique ID (for testing)      | Description                                                                                      |
| :------------------------- | :---------------------- | :--------------------------- | :----------------------------------------------------------------------------------------------- |
| `SimulateMCPButton`        | `@Composable` Button    | `btn_simulate_mcp`           | Debug Dashboard button that calls `simulateMcpCollision()` in the ViewModel.                     |
| `ExternalImmutableCard`    | `@Composable` Card      | `card_habit_external_{id}`   | `ClarityBlue` background, 🔒 lock icon, dark typography. Signals an unmodifiable external block. |
| `PendingReallocationCard`  | `@Composable` Card      | `card_habit_pending_{id}`    | `DarkSurface` background at 50% alpha, 🕐 clock icon. Signals a displaced habit awaiting action. |
| `Native Notification`      | Android System UI        | N/A (OS-rendered)            | High-priority contextual notification with empathetic text and "Re-locate to 7:30 PM" action.    |

---

## 9. Technical Constraints

1. **No Real MCP Client**: MCP communication is simulated via direct `ExternalEvent` object injection into the ViewModel. No actual socket or HTTP client is opened. See `Architectural_Context.md §2`.
2. **No WebSockets**: Supabase synchronization is considered instantly successful upon local state mutation. See `Tech-Stack_Context.md §5`.
3. **Calendar Priority Rule Contract**: Events with `isImmutable = true` always take precedence over internal blocks at the same `scheduledTime`. This rule must be implemented exactly as specified in §5.
4. **NotificationConfig Reuse**: Uses `NotificationConfig.NOTIFICATION_ID_MCP = 1002` and `CHANNEL_ID = "rout_in_behavioral_alerts"` from SPEC03 §5. The channel must already exist (created in SPEC01).
5. **Action Intent Extra**: The `PendingIntent` for "Re-locate to 7:30 PM" must carry `EXTRA_HABIT_ID = 106` and `EXTRA_NOTIFICATION_ID = 1002` so `NotificationActionReceiver` can correctly identify the target block.
6. **Visual Differentiation**: External immutable blocks must be visually distinguishable from internal blocks via the `ExternalImmutableCard` variant (`ClarityBlue` background + lock icon). `PendingReallocationCard` must indicate the displaced state with reduced opacity.
7. **State Isolation**: All event ingestion, collision detection, state mutation, and notification dispatch logic resides exclusively in the ViewModel. `@Composable` functions observe `StateFlow<List<HabitBlock>>` only.
8. **`POST_NOTIFICATIONS` Permission**: Required on API 33+. Already handled in `MainActivity` per SPEC03 §9.8.
9. **Demo Precondition Reset (Order-Independence)**: `simulateMcpCollision()` must begin by **restoring** `habitBlocks[id=106]` to its required precondition state (`scheduledTime = "18:00"`, `status = "PENDING"`, `isImmutable = false`) and removing any previously inserted external exam blocks before executing the Calendar Priority Rule. This guarantees that pressing "Simulate MCP Collision" always produces the correct collision — even if the voice command (SPEC02) already shifted the Gym to 19:00.

---

## 10. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Pressing "Simulate MCP Collision" inserts the exam block as `isImmutable=true` at `scheduledTime = "18:00"`. | Unit Test      |
| 2  | The `"Gym / Workout"` block (id=106) transitions to `status = "PENDING_REALLOCATION"`.                    | Unit Test        |
| 3  | The exam block is displayed with the `ExternalImmutableCard` variant (`ClarityBlue`, 🔒 icon).             | UI Test          |
| 4  | The gym block is displayed with the `PendingReallocationCard` variant (50% opacity, 🕐 icon).              | UI Test          |
| 5  | A contextual heads-up notification is dispatched with the empathetic Spanish text.                         | Device Test      |
| 6  | The notification contains a "Re-locate to 7:30 PM" action button backed by a `PendingIntent`.              | Device Test      |
| 7  | Confirming "Re-locate to 7:30 PM" sets `scheduledTime = "19:30"` and `status = "PENDING"` on the gym block.| Unit Test       |
| 8  | The notification is dismissed after confirmation (`NOTIFICATION_ID_MCP` is cancelled).                     | Device Test      |
| 9  | The dashboard shows exam at 18:00 and gym at 19:30 in correct chronological order.                        | UI Test          |
| 10 | No real MCP client, WebSocket, or HTTP client is imported.                                                 | Build Verify     |
