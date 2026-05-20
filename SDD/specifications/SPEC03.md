# SPEC03: Proactive Suggestion Intervention

## 1. Traceability

| Attribute | Value |
| :--- | :--- |
| **Source Feature** | `features/ProactiveSuggestion.feature` |
| **Architectural Ref** | `Architectural_Context.md §3.2` — Flow B |
| **Depends On** | **SPEC01**, **SPEC02** (shared data model) |
| **Priority** | **P1 — Core Feature** |

---

## 2. Objective

Specify how the app detects a habit in "Critical Friction" state (skipped 3 times) and replaces the standard alarm with an **empathetic interactive notification** using a disruptive haptic pattern and one-tap rescheduling. Demonstrates: **from rigid alarms to proactive suggestions**.

---

## 3. Preconditions

| # | Precondition |
| :- | :--- |
| 1 | The telemetry engine tracks that "Reading Block" has been skipped 3 consecutive times. |
| 2 | The habit state is set to `HabitStatus.FRICTION` in the in-memory collection. |
| 3 | The scheduled time for the high-friction habit has arrived. |

---

## 4. MVP Simulation Strategy

| Production Component | MVP Simulation |
| :--- | :--- |
| Telemetry Engine (skip analytics) | Simulated: "Simulate Critical Friction" button in Debug Dashboard. |
| Cloud notification orchestration | Real: native `NotificationManager` + `NotificationChannel` SDK APIs. |
| Adaptive haptic engine | Real: `android.os.Vibrator` with custom pattern on real hardware. |
| Room Database mutation | Simulated: direct mutation of `MutableList<HabitBlock>` in ViewModel. |

---

## 5. Data Model Extensions

Reuses `HabitBlock` and `HabitStatus` from **SPEC02**. Extensions:

```kotlin
data class FrictionMetrics(
    val habitId: String,
    val consecutiveSkips: Int = 0,
    val isCritical: Boolean = consecutiveSkips >= 3
)

object NotificationConfig {
    const val CHANNEL_ID = "rout_in_proactive"
    const val CHANNEL_NAME = "Rout-In Proactive Suggestions"
    const val NOTIFICATION_ID = 1001
    val HAPTIC_PATTERN = longArrayOf(0, 250, 200, 250, 150, 400)
}
```

---

## 6. State Machine Specification

Reference: `Architectural_Context.md §3.2 — Flow B`.

```
[Debug Dashboard]
    │
    ├─ User clicks "Simulate Critical Friction"
    │   ├─► targetHabit.status = HabitStatus.FRICTION
    │   ├─► Build NotificationChannel (IMPORTANCE_HIGH)
    │   ├─► Vibrator.vibrate(pattern: {0,250,200,250,150,400})
    │   ├─► Fire native Notification with empathetic text:
    │   │     "Hola. Notamos que este horario te ha costado trabajo
    │   │      últimamente. No te preocupes, vamos a tu ritmo.
    │   │      ¿Prefieres mover la lectura a hoy a las 6:30 PM
    │   │      o prefieres que lo intentemos el sábado por la mañana?"
    │   └─► addAction("Move to 6:30 PM", pendingIntent)
    │
    └─ User taps "Move to 6:30 PM" on notification
        ├─► BroadcastReceiver intercepts PendingIntent
        ├─► targetHabit.scheduledTime = 18:30
        ├─► targetHabit.status = HabitStatus.ACTIVE
        ├─► NotificationManager.cancel(NOTIFICATION_ID)
        └─► Dashboard reflects updated state
```

---

## 7. Seed Data

| Block Name | Scheduled Time | Status | Consecutive Skips |
| :--- | :--- | :--- | :--- |
| Reading Block | 5:30 PM | FRICTION | 3 |

---

## 8. UI Components Required

| Component ID | Type | Description |
| :--- | :--- | :--- |
| `DebugDashboard` | `@Composable` Section | Panel with manual simulation buttons for system events. |
| `SimulateFrictionButton` | `@Composable` Button | Triggers the critical friction event in the ViewModel. |
| `FrictionHabitCard` | `@Composable` Card | Visual variant with warning styles (amber/red, ⚠️ icon). |
| Native Notification | Android System UI | Real notification with empathetic text and action button. |

---

## 9. Technical Constraints

1. **Real Hardware APIs**: Uses **real** native SDK APIs: `NotificationManager`, `NotificationChannel`, `Vibrator` (`Tech-Stack_Context.md §4`).
2. **PendingIntent Flow**: The notification action button uses a registered `BroadcastReceiver` that communicates back to the ViewModel.
3. **Haptic Pattern**: `{0, 250, 200, 250, 150, 400}` is defined in `Architectural_Context.md §3.2` — must be implemented exactly.
4. **NotificationChannel Required**: Android 8.0+ (minSdk 26) requires channel creation before the first notification.

---

## 10. Acceptance Criteria

| # | Criterion | Verification |
| :- | :--- | :--- |
| 1 | Pressing "Simulate Critical Friction" changes "Reading Block" to `FRICTION` status. | Unit Test |
| 2 | The habit card visually reflects the friction state (warning color). | UI Test |
| 3 | A real notification is dispatched with the specified empathetic text. | Device Test |
| 4 | The device executes the custom haptic pattern. | Device Test |
| 5 | Pressing "Move to 6:30 PM" reschedules the habit to 6:30 PM. | Unit Test |
| 6 | The notification is dismissed after confirmation. | Device Test |
| 7 | The dashboard reflects the updated schedule without manual refresh. | UI Test |
