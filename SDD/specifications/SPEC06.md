# SPEC06: Interactive Habit Gamification and Completion

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/InteractiveHabitGamification.feature`                                                                          |
| **Architectural Ref** | `Architectural_Context.md §3.5` — Flow E: Interactive Habit Completion                                                   |
| **Data Ref**          | `Mock_Data_Seed_Context.md`                                                                                              |
| **UI Ref**            | `UI_Guide_Context.md §4.1` — Card Component Layouts & Background Shading                                                 |
| **Depends On**        | **SPEC01**, **SPEC02** (`HabitBlock` model)                                                                              |
| **Priority**          | **P1 — Core Feature**                                                                                                    |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Specify the interactive habit completion flow. This feature provides immediate sensory reinforcement to easily distracted users, rewarding tracking consistency. By tapping the pending habit, the card dynamically transitions its theme, signaling task completion while maintaining strict accessibility guidelines.

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | The user is actively viewing their current routine feed on the Main Dashboard.                                  |
| 2  | The target habit block has a status flag set to `PENDING`.                                                      |

---

## 4. UI Design Extensions

Leverages design tokens specified in `UI_Guide_Context.md`:

- **Completed Card Background:** `RoutInColors.WellbeingMint` (0xFFDCF5EC).
- **Completed Card Typography:** `RoutInColors.DeepPurpleNavy` (0xFF1C1B29) ensures high-contrast readability on pastel backgrounds.
- **Animation:** Background tint must smoothly animate using standard Jetpack Compose animation APIs (`animateColorAsState`).

---

## 5. Data Model & State Machine

The interaction modifies the existing `HabitBlock` state. 

**Mutation Contract:**
1. The UI detects a tap event on the habit card container or the completion icon.
2. The UI invokes an intent action (e.g., `viewModel.markHabitCompleted(id)`).
3. The `ViewModel` atomically mutates the specific habit's `status` to `COMPLETED`.
4. The list is re-emitted, triggering the Compose redraw.

**State Machine Specification (Reference `Architectural_Context.md §3.5`):**

```
[MainDashboardScreen → Habit Card]
    │
    ├─ User clicks the interaction target (Card or Icon)
    │   │
    │   ├─► viewModel.markHabitCompleted(targetId)
    │   │
    │   ├─► MUTATE DATA STATE:
    │   │     habitBlocks.find { it.id == targetId }?.status = "COMPLETED"
    │   │
    │   └─► UI RENDER UPDATE (reactive via StateFlow):
    │         • animateColorAsState transitions from DarkSurface to WellbeingMint
    │         • Text color swaps to DeepPurpleNavy instantly
```

---

## 6. Technical Constraints

1. **Accessibility Compliance:** When the background shifts to `WellbeingMint`, all foreground text within the card must instantly flip to `DeepPurpleNavy`. Using `OffWhiteSerenity` on a Mint background violates WCAG contrast guidelines and will cause visual fatigue.
2. **Animation:** The color transition should not be abrupt. Use `animateColorAsState` to smoothly interpolate between `DarkSurface` and `WellbeingMint`.
3. **No Network Calls:** The state mutation must remain entirely in-memory within the `ViewModel` per the Offline-First MVP architecture.
4. **State Isolation:** Compose must remain declarative. The tap must simply invoke the ViewModel function and respond to the emitted state change.

---

## 7. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | Tapping a `PENDING` habit updates its state to `COMPLETED` in the `ViewModel`.                             | Unit Test        |
| 2  | The background color of the card animates smoothly towards `WellbeingMint`.                                | UI Test          |
| 3  | The typography color inside the completed card instantly changes to `DeepPurpleNavy`.                      | UI Test          |
| 4  | Only the tapped card changes state; other cards remain unaffected.                                         | UI Test          |
