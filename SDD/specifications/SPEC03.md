# SPEC03: Proactive Suggestion Intervention

## 1. Traceability

| Attribute             | Value                                                                                               |
| :-------------------- | :-------------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/ProactiveSuggestion.feature`                                                              |
| **Architectural Ref** | `Architectural_Context.md §3.2` — Flow B: Proactive Habit Re-scheduling                            |
| **Data Ref**          | `Mock_Data_Seed_Context.md §3` — Behavioral suggestion context strings & notification payload       |
| **UI Ref**            | `UI_Guide_Context.md §2, §4` — Color palette (OptimismYellow for friction), component rules        |
| **API Ref**           | `Native_API_Recipes_Context.md §2, §3, §4` — Channel creation, interactive notification, haptics   |
| **Tech Ref**          | `Tech-Stack_Context.md §4, §5` — Native SDK APIs, prohibited libraries                             |
| **Depends On**        | **SPEC01** (channel bootstrap in `MainActivity`); **SPEC02** (shared `HabitBlock` model, `UiState`) |
| **Priority**          | **P1 — Core Feature**                                                                               |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                   |

---

## 2. Objective

Specify how the app detects a habit in "Critical Friction" state (skipped 3 times) and replaces the standard alarm with an **empathetic interactive notification** using a disruptive haptic pattern and one-tap rescheduling. Demonstrates: **from rigid alarms to proactive suggestions** (`General_Project_Context.md §3.2`).

---

## 3. Preconditions

| #  | Precondition                                                                                                          |
| :- | :-------------------------------------------------------------------------------------------------------------------- |
| 1  | Onboarding has been completed (SPEC01) and `MainActivity.onCreate()` has already created the `NotificationChannel`.   |
| 2  | The `"Reading Block"` habit (id = `108`) exists in the ViewModel's in-memory list with `status = "PENDING"`.          |
| 3  | The telemetry engine tracks that "Reading Block" has been skipped 3 consecutive times (simulated via debug trigger).  |

---

## 4. MVP Simulation Strategy

| Production Component                  | MVP Simulation                                                                             |
| :------------------------------------ | :----------------------------------------------------------------------------------------- |
| Telemetry Engine (skip analytics)     | Simulated: "Simulate Critical Friction" button in `DebugDashboard`.                        |
| Cloud notification orchestration      | **Real**: native `NotificationManager` + `NotificationChannel` SDK APIs.                  |
| Adaptive haptic engine                | **Real**: `android.os.Vibrator` / `VibratorManager` with custom pattern on real hardware. |
| Room Database mutation                | Simulated: direct mutation of `MutableList<HabitBlock>` in ViewModel.                     |

---

## 5. Data Model Extensions

> Reuses canonical `HabitBlock` (defined in SPEC01 §5 / `Mock_Data_Seed_Context.md §1`) and `UiState` (defined in SPEC02 §5). The models below are SPEC03-specific extensions only.

```kotlin
// file: data/model/FrictionModels.kt

data class FrictionMetrics(
    val habitId: Int,                               // Matches HabitBlock.id
    val consecutiveSkips: Int = 0,
    val isCritical: Boolean = consecutiveSkips >= 3
)
```

**Notification Configuration** — single source of truth for all notification constants across the entire app:

```kotlin
// file: data/model/NotificationConfig.kt

object NotificationConfig {
    // CANONICAL channel ID — must match NotificationHelper.CHANNEL_ID in Native_API_Recipes_Context.md §2
    const val CHANNEL_ID = "rout_in_behavioral_alerts"
    const val CHANNEL_NAME = "Behavioral Interventions"

    // Notification IDs
    const val NOTIFICATION_ID_FRICTION  = 1001   // SPEC03: Proactive suggestion
    const val NOTIFICATION_ID_MCP       = 1002   // SPEC04: External calendar collision

    // Haptic pattern: { Delay, ON, OFF, ON, OFF, ON } in milliseconds
    val HAPTIC_PATTERN = longArrayOf(0, 250, 200, 250, 150, 400)
    val HAPTIC_AMPLITUDES = intArrayOf(0, 255, 0, 180, 0, 255) // API 26+

    // BroadcastReceiver action strings
    const val ACTION_MOVE_HABIT_630  = "ACTION_MOVE_HABIT_630"   // SPEC03
    const val ACTION_RELOCATE_730   = "ACTION_RELOCATE_730"      // SPEC04

    // Intent extras
    const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
    const val EXTRA_HABIT_ID        = "HABIT_ID"
}
```

> ⚠️ **Constraint:** `CHANNEL_ID = "rout_in_behavioral_alerts"` is the canonical value. Any previous references to `"rout_in_proactive"` in earlier drafts are **deprecated** and must be replaced with this value throughout the codebase.

---

## 6. State Machine Specification

Reference: `Architectural_Context.md §3.2 — Flow B`.

```
[MainDashboardScreen → DebugDashboard section]
    │
    ├─ User clicks "Simulate Critical Friction"
    │   │
    │   ├─► viewModel.simulateCriticalFriction(habitId = 102)
    │   │
    │   ├─► MUTATE DATA STATE:
    │   │     habitBlocks[id=102].status = "FRICTION"
    │   │     (UI re-renders FrictionHabitCard for this block)
    │   │
    │   ├─► TRIGGER HAPTIC PATTERN (real hardware):
    │   │     triggerSensoryDisruptiveVibration(context)
    │   │     // Pattern: { 0, 250, 200, 250, 150, 400 } — see Native_API_Recipes_Context.md §4
    │   │
    │   ├─► DISPATCH NATIVE NOTIFICATION (real hardware):
    │   │     NotificationManager.notify(
    │   │       NOTIFICATION_ID_FRICTION,
    │   │       Notification {
    │   │         channel: "rout_in_behavioral_alerts"
    │   │         priority: IMPORTANCE_HIGH (heads-up banner)
    │   │         title: "Rout-In Assistant"
    │   │         text: "Hola. Notamos que este horario te ha costado trabajo
    │   │                últimamente. No te preocupes, vamos a tu ritmo.
    │   │                ¿Prefieres mover la lectura a hoy a las 6:30 PM
    │   │                o prefieres que lo intentemos el sábado por la mañana?"
    │   │         action[0]: Label="Move to 6:30 PM"
    │   │                     PendingIntent → NotificationActionReceiver
    │   │                     (ACTION_MOVE_HABIT_630)
    │   │         action[1]: Label="Cancel"
    │   │                     PendingIntent → NotificationActionReceiver
    │   │                     (dismiss only, no state change)
    │   │       }
    │   │     )
    │   │
    │   └─► [Waiting for user interaction on notification tray]
    │
    └─ User taps "Move to 6:30 PM" on notification
        │
        ├─► NotificationActionReceiver.onReceive(ACTION_MOVE_HABIT_630)
        │
        ├─► NotificationManager.cancel(NOTIFICATION_ID_FRICTION)
        │
        ├─► context.sendBroadcast(Intent("UPDATE_UI_STATE_HABIT_SHIFT"))
        │   // RoutInViewModel listens via a registered BroadcastReceiver
        │
        ├─► MUTATE DATA STATE:
        │     habitBlocks[id=102].scheduledTime = "18:30"
        │     habitBlocks[id=102].status = "REALLOCATED"
        │
        └─► [MainDashboardScreen reflects updated schedule — no manual refresh]
```

---

## 7. Seed Data

Reuses the expanded seed from SPEC02 §7. Key entry for this flow:

| ID  | Block Name    | Scheduled Time | Status     | Consecutive Skips |
| :-- | :------------ | :------------- | :--------- | :---------------- |
| 108 | Reading Block | 17:00          | `PENDING`  | 3 (simulated)     |

> **Note (ID uniqueness):** The canonical expanded seed uses `id = 102` for "Almuerzo". "Reading Block" is assigned `id = 108` to guarantee every entry in the combined dataset has a globally unique identifier, preventing a `LazyColumn` duplicate-key `IllegalArgumentException` crash.

After simulation trigger:

| ID  | Block Name    | Scheduled Time | Status        |
| :-- | :------------ | :------------- | :------------ |
| 108 | Reading Block | 18:30          | `REALLOCATED` |

---

## 8. UI Components Required

Design tokens from `UI_Guide_Context.md §2, §4`:
- Friction card background: `RoutInColors.OptimismYellow` (with text swapping to `DeepPurpleNavy`)
- Card shape: `RoundedCornerShape(24.dp)`

| Component ID              | Type                    | Unique ID (for testing)    | Description                                                                            |
| :------------------------ | :---------------------- | :------------------------- | :------------------------------------------------------------------------------------- |
| `DebugDashboard`          | `@Composable` Section   | `section_debug`            | Collapsible panel at the bottom of `MainDashboardScreen` with simulation buttons.      |
| `SimulateFrictionButton`  | `@Composable` Button    | `btn_simulate_friction`    | Triggers `simulateCriticalFriction()` in the ViewModel.                                |
| `FrictionHabitCard`       | `@Composable` Card      | `card_habit_friction_{id}` | Visual variant of `HabitBlockCard`: `OptimismYellow` background, ⚠️ icon, dark text.  |
| `Native Notification`     | Android System UI        | N/A (OS-rendered)          | High-importance heads-up notification with empathetic text and two action buttons.     |

---

## 9. Technical Constraints

1. **Real Hardware APIs**: Uses **real** native SDK APIs: `NotificationManager`, `NotificationChannel`, `Vibrator`/`VibratorManager`. See `Tech-Stack_Context.md §4` and `Native_API_Recipes_Context.md §2, §3, §4`.
2. **Canonical Channel ID**: The channel `"rout_in_behavioral_alerts"` must already exist (created by SPEC01's `MainActivity.onCreate()`) before this notification fires. SPEC03 does **not** re-create the channel.
3. **PendingIntent Architecture**: The notification action button uses `NotificationActionReceiver` (a registered `BroadcastReceiver`) that communicates back to the ViewModel via a local `sendBroadcast()`. See `Native_API_Recipes_Context.md §3`.
4. **PendingIntent Flags**: Use `PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT` on API 23+. See `Native_API_Recipes_Context.md §3`.
5. **Haptic Pattern**: `{0, 250, 200, 250, 150, 400}` with amplitudes `{0, 255, 0, 180, 0, 255}`. Must be implemented exactly per `Architectural_Context.md §3.2` and `Native_API_Recipes_Context.md §4`.
6. **Vibrator API branching**: Use `VibratorManager` for API 31+, fall back to deprecated `Vibrator` for API 26–30. See `Native_API_Recipes_Context.md §4`.
7. **State Isolation**: Haptic trigger, notification dispatch, and list mutation reside exclusively in the ViewModel or its helper utilities. `@Composable` functions are declarative observers only.
8. **`POST_NOTIFICATIONS` Permission**: For API 33+ (Android 13), the app must declare and request `android.permission.POST_NOTIFICATIONS` at runtime. Handle in `MainActivity`.
9. **Demo Precondition Reset (Order-Independence)**: `simulateCriticalFriction()` must begin by **restoring** `habitBlocks[id=108]` to its required precondition state (`scheduledTime = "17:00"`, `status = "PENDING"`) before applying any mutation or firing the notification. This guarantees the flow produces the correct result regardless of whether the voice command (SPEC02) or MCP collision (SPEC04) was executed first.

---

## 10. Acceptance Criteria

| #  | Criterion                                                                                          | Verification     |
| :- | :------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Pressing "Simulate Critical Friction" changes `habitBlocks[id=108].status` to `"FRICTION"`.        | Unit Test        |
| 2  | The habit card visually reflects the friction state (`OptimismYellow` background, ⚠️ icon).        | UI Test          |
| 3  | The device executes the custom haptic pattern `{0,250,200,250,150,400}`.                           | Device Test      |
| 4  | A real heads-up notification is dispatched with the specified empathetic text in Spanish.          | Device Test      |
| 5  | The notification contains a "Move to 6:30 PM" action button backed by a `PendingIntent`.           | Device Test      |
| 6  | Pressing "Move to 6:30 PM" sets `habitBlocks[id=108].scheduledTime = "18:30"` and `status = "REALLOCATED"`. | Unit Test        |
| 7  | The notification is dismissed after confirmation.                                                   | Device Test      |
| 8  | The dashboard reflects the updated schedule without a manual refresh.                              | UI Test          |
| 9  | No network clients, Room, or cloud SDKs are imported.                                              | Build Verify     |
