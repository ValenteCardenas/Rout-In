# SPEC07: Full In-Memory Habit CRUD (Update & Delete)

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/FullHabitCRUD.feature` (TBD)                                                                                   |
| **Architectural Ref** | `Architectural_Context.md §3.4`                                                                                          |
| **Depends On**        | **SPEC05** (In-Memory Habit Creation), **SPEC06** (Gamification)                                                         |
| **Priority**          | **P1 — Core Feature**                                                                                                    |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Complete the manual CRUD lifecycle for habit blocks by introducing **Update (Edit)** and **Delete** capabilities. This allows users to long-press any habit card to dynamically modify its properties (Name, Time, Duration, and Immutable status) or remove it. Crucially, modifying a habit's time or immutable status will trigger the ViewModel's collision resolution algorithm, visually demonstrating the state machine's adaptability to the jury.

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | The user is actively viewing their current routine feed on the Main Dashboard.                                  |
| 2  | The ViewModel's in-memory list contains at least one habit block.                                               |

---

## 4. UI Gestures & Component Design

1. **Card Gesture (Long Press):**
   - Replace the `onClick` parameter in `HabitBlockCard` with an explicit double-gesture wrapper using `Modifier.combinedClickable`:
     - `onClick`: Toggles completion (SPEC06).
     - `onLongClick`: Triggers the `EditHabitDialog` prepopulated with the selected block's data.

2. **EditHabitDialog Component:**
   - Visual clone of `AddHabitDialog` but titled "Editar Hábito".
   - **Inputs:**
     - Habit Name (`OutlinedTextField`).
     - Scheduled Time (Native `TimePickerDialog`).
     - Duration in minutes (`OutlinedTextField`).
   - **New Toggles/Actions:**
     - **"Fijo" Toggle (Switch):** A UI element allowing the user to mark the habit as `isImmutable = true`. (Crucial for demonstrating schedule anchoring).
     - **Delete Button:** A `TextButton` placed on the bottom-left with a soft red/coral accent (e.g., `Color(0xFFE57373)`) to trigger deletion.
     - **Confirm Button:** Applies the edits.

---

## 5. State Machine & ViewModel Contract

The `RoutInViewModel` must expose two new mutation functions. Both functions must reuse the existing `mutateHabitBlocks` to guarantee thread-safety, state emission, and automatic collision resolution.

**Contract 1: Update Habit**
```kotlin
fun updateCustomHabit(id: Int, name: String, time: String, duration: Int, isImmutable: Boolean) {
    mutateHabitBlocks {
        val index = indexOfFirst { it.id == id }
        if (index != -1) {
            val old = this[index]
            this[index] = old.copy(
                name = name,
                scheduledTime = time,
                durationMinutes = duration,
                isImmutable = isImmutable
            )
        }
    }
}
```

**Contract 2: Delete Habit**
```kotlin
fun deleteCustomHabit(id: Int) {
    mutateHabitBlocks {
        removeAll { it.id == id }
    }
}
```

---

## 6. Technical Constraints

1. **Immutable Re-anchoring:** Toggling `isImmutable` via the dialog and saving must immediately anchor the block at its assigned time, forcing `resolveCollisions` to cascade flexible blocks around it.
2. **Gesture Conflicts:** `Modifier.combinedClickable` requires the `@OptIn(ExperimentalFoundationApi::class)` annotation in Compose. Ensure it does not swallow vertical scrolling events from the parent `LazyColumn`.
3. **Data Model Immutability:** Do not change `val name` or `val durationMinutes` to `var` in the `HabitBlock` model. The `mutateHabitBlocks` logic must create copies of the object `old.copy(...)`, adhering strictly to Kotlin immutable data paradigms while updating the list reference.

---

## 7. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Long-pressing a habit card successfully opens the Edit dialog with pre-filled data.                        | UI Test          |
| 2  | Tapping "Eliminar" removes the habit from the list and re-calculates the schedule.                         | UI / Unit Test   |
| 3  | Saving an edit updates the card UI instantly and triggers `resolveCollisions`.                             | UI Test          |
| 4  | Modifying a habit to be "Fijo" (Immutable) locks its time, shifting flexible habits correctly.             | UI Test          |
