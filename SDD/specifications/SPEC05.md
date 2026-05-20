# SPEC05: In-Memory Habit Creation

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/InMemoryHabitCreation.feature`                                                                                 |
| **Architectural Ref** | `Architectural_Context.md §3.4` — Flow D: Manual Habit Creation (In-Memory Fallback)                                     |
| **Data Ref**          | `Mock_Data_Seed_Context.md`                                                                                              |
| **UI Ref**            | `UI_Guide_Context.md`                                                                                                    |
| **Depends On**        | **SPEC01**, **SPEC02** (`HabitBlock` model)                                                                              |
| **Priority**          | **P1 — Core Feature**                                                                                                    |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Specify the manual in-memory habit creation flow. This allows users to add custom routines directly from the dashboard when sudden changes occur, ensuring offline-first adaptability without relying on cloud connections.

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | Onboarding has been completed.                                                                                  |
| 2  | The user is viewing the Main Dashboard.                                                                         |
| 3  | The ViewModel's in-memory list contains the baseline seed habits.                                               |

---

## 4. MVP Simulation Strategy

| Production Component                  | MVP Simulation                                                                                   |
| :------------------------------------ | :----------------------------------------------------------------------------------------------- |
| Cloud Database Storage                | Simulated: In-memory list collection inside the ViewModel.                                       |
| Unique ID Generation (UUID)           | Simulated: `maxOfOrNull { it.id } + 1` to generate a sequential unique integer ID.               |

---

## 5. Data Model Extensions

Reuses canonical `HabitBlock` (SPEC01 §5) and `UiState` (SPEC02 §5).

**Creation Contract** (enforced in `RoutInViewModel`):

```kotlin
fun addCustomHabit(name: String, time: String, duration: Int) {
    val newId = (_habitBlocks.value.maxOfOrNull { it.id } ?: 0) + 1
    val newHabit = HabitBlock(
        id = newId,
        name = name,
        scheduledTime = time,
        durationMinutes = duration,
        status = "PENDING",
        isImmutable = false,
        source = "INTERNAL"
    )
    mutateHabitBlocks { add(newHabit) }
}
```

---

## 6. State Machine Specification

Reference: `Architectural_Context.md §3.4 — Flow D`.

```
[MainDashboardScreen]
    │
    ├─ User taps Floating Action Button
    │   │
    │   ├─► Display Add Habit Dialog
    │
    ├─ User fills fields and taps "Confirm"
    │   │
    │   ├─► viewModel.addCustomHabit("Repasar Sistemas Operativos", "20:00", 60)
    │   │
    │   ├─► CALCULATE UNIQUE ID:
    │   │     val newId = (_habitBlocks.value.maxOfOrNull { it.id } ?: 0) + 1
    │   │
    │   ├─► MUTATE DATA STATE:
    │   │     habitBlocks.add(newHabit)
    │   │     resolveCollisions(habitBlocks)
    │   │
    │   └─► UI RENDER UPDATE (reactive via StateFlow):
    │         • Dialog dismisses immediately
    │         • LazyColumn smoothly animates and renders the new item at the proper chronological position
```

---

## 7. Technical Constraints

1. **Unique ID Generation**: The ID generation logic must strictly use `(_habitBlocks.value.maxOfOrNull { it.id } ?: 0) + 1` to prevent duplicate key crashes in Compose `LazyColumn`s.
2. **State Isolation**: The dialog logic must only gather parameters and invoke `addCustomHabit` on the `ViewModel`. No state mutation is permitted in the view.
3. **Collision Resolution**: Adding a new habit must trigger the existing `resolveCollisions` algorithm to cascade any conflicts smoothly.
4. **FAB Slot Refactor**: The `Scaffold` `floatingActionButton` parameter only accepts a single composable node. To support both the Add Habit and Microphone FABs without hierarchical crashes, they must be wrapped in a `Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End)`.

---

## 8. AlertDialog Implementation Plan

- **State Management**: Create a boolean `MutableState` (e.g., `showAddDialog`) in `MainDashboardScreen` to track visibility.
- **Component**: Build a Material Design 3 `AlertDialog`.
- **Inputs**: Use `OutlinedTextField` components to capture "Habit Name" and "Duration" (numeric). For "Scheduled Time" (HH:mm format), use a read-only `OutlinedTextField` that opens a native `android.app.TimePickerDialog` on tap.
- **Actions**:
  - **Confirm**: Parse inputs, invoke `viewModel.addCustomHabit(name, time, duration)`, and set `showAddDialog = false`.
  - **Dismiss**: Set `showAddDialog = false` without calling the view model.

---

## 9. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Filling the Add Habit Dialog and pressing Confirm adds a new item to the in-memory list.                   | UI / Unit Test   |
| 2  | The new block is given a unique sequential ID preventing Compose duplicate key crashes.                      | Unit Test        |
| 3  | The UI feed automatically updates and renders the new habit without requiring a refresh.                   | UI Test          |
