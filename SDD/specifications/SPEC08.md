# SPEC08: AI Copilot Progress Hub & Dashboard Declutter

## 1. Traceability

| Attribute             | Value                                                                                                                    |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Architectural Ref** | `Architectural_Context.md §3` — Core State Machine                                                                      |
| **UI Ref**            | `UI_Guide_Context.md §4.1` — Card Component Layouts & Background Shading                                                |
| **Depends On**        | **SPEC01–SPEC04** (State flags), **SPEC06** (Completion toggle)                                                          |
| **Priority**          | **P2 — Polish & Presentation Impact**                                                                                    |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                                        |

---

## 2. Objective

Replace the static text-only dashboard header with a premium **AI Copilot & Progress Hub** card that dynamically adapts its coaching message and progress metrics based on the current state of the habit list. Simultaneously, reduce visual clutter on the main feed by making the Debug Dashboard **collapsible** (starts minimized, expands on tap).

---

## 3. Preconditions

| #  | Precondition                                                                                                    |
| :- | :-------------------------------------------------------------------------------------------------------------- |
| 1  | Onboarding has been completed and the user is viewing the Main Dashboard.                                       |
| 2  | The ViewModel exposes the reactive `habitBlocks` state containing the current list.                             |

---

## 4. Component A: AI Copilot Progress Card

This card **replaces** the existing `DashboardHeader` composable. It does NOT add a new section — it upgrades the existing header slot.

### 4.1 Visual Layout

```
┌─────────────────────────────────────────────────────┐
│  Buenos días, Gabriel 👋                            │
│  Tu agenda de hoy · 20/05/2026                      │
│                                                     │
│  ████████████████░░░░░░░░  3/5 completados · 60%    │
│  🔥 Racha de 5 días                                 │
│                                                     │
│  💡 "Tu siguiente reto es el Reading Block a las    │
│      17:00. ¡Tú puedes!"                            │
└─────────────────────────────────────────────────────┘
```

- **Container**: `Card` with `RoundedCornerShape(24.dp)`, background using a subtle gradient from `RoutInColors.DarkSurface` to `RoutInColors.DeepPurpleNavy`.
- **Progress Bar**: `LinearProgressIndicator` styled with `RoutInColors.VibrantGreenEmphasis` track and `RoutInColors.DarkSurface.copy(alpha = 0.3f)` background. Width: `Modifier.fillMaxWidth()`.
- **Streak Counter**: Hardcoded to `"🔥 Racha de 5 días"` for the MVP demo. Exposed as a `val streakDays: Int = 5` inside the ViewModel for easy tuning.
- **AI Message**: Dynamic text block (see §4.2).

### 4.2 Dynamic AI Coaching Messages

The coaching message is computed **reactively** inside the ViewModel as a derived `State<String>`. It evaluates the current `habitBlocks` list against a priority-ordered set of local flags:

**Priority Resolution Order** (first match wins):

| Priority | Condition (evaluated on `habitBlocks`)                                       | Message                                                                                                                      |
| :------- | :--------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------- |
| 1        | Any block has `status == FRICTION`                                           | `"Noto que el {name} te está costando hoy. No te satures; podemos moverlo para que respires."` |
| 2        | Any block has `status == PENDING_REALLOCATION`                               | `"He protegido tu bloque académico. ¿Deseas reubicar {name} para mantener tu bienestar?"` |
| 3        | Any block has `status == REALLOCATED`                                        | `"Listo, ya reorganicé tu agenda. Tu {name} ahora está a las {time}. ¡Sigue así!"` |
| 4        | All non-immutable blocks are `COMPLETED`                                     | `"🎉 ¡Increíble, Gabriel! Completaste todas tus rutinas hoy. Descansa bien, mañana seguimos."` |
| 5        | `completedCount > 0` (partial progress)                                      | `"Llevas {completed}/{total} completados. Tu siguiente reto es {nextPending.name} a las {time}. ¡Tú puedes!"` |
| 6        | Default (no completions yet)                                                 | `"¡Buen día, Gabriel! Tu agenda está lista. Empieza con {firstPending.name} a las {time}."` |

**Implementation Contract** (inside `RoutInViewModel`):

```kotlin
val coachingMessage: State<String>
    get() {
        val blocks = _habitBlocks.value
        val frictionBlock = blocks.find { it.status == StatusConstants.FRICTION }
        if (frictionBlock != null) return mutableStateOf(
            "Noto que ${frictionBlock.name} te está costando hoy. No te satures; podemos moverlo para que respires."
        )
        // ... continue priority chain
    }
```

> **Note:** Since `_habitBlocks` is a `MutableState`, the derived property will automatically recompose whenever the list changes. However, for clean reactivity, the agent should implement this as a `derivedStateOf` block inside the ViewModel to avoid redundant re-evaluations.

### 4.3 Progress Computation

```kotlin
val completionProgress: State<Float>
    get() {
        val blocks = _habitBlocks.value.filter { !it.isImmutable || it.source == HabitBlock.Source.INTERNAL }
        val completed = blocks.count { it.status == StatusConstants.COMPLETED }
        return mutableStateOf(if (blocks.isEmpty()) 0f else completed.toFloat() / blocks.size)
    }

val completionLabel: State<String>
    get() {
        val blocks = _habitBlocks.value.filter { !it.isImmutable || it.source == HabitBlock.Source.INTERNAL }
        val completed = blocks.count { it.status == StatusConstants.COMPLETED }
        return mutableStateOf("$completed/${blocks.size} completados")
    }
```

> **Agent Implementation Note:** These should be `derivedStateOf` blocks for performance, NOT recomputed on every access.

---

## 5. Component B: Collapsible Debug Dashboard

The existing `DebugDashboard` composable must be wrapped in an expandable/collapsible container to reduce visual noise during the demo flow.

### 5.1 Behavior

- **Default State**: Collapsed — shows only a single-line header row: `"🐛 Debug Dashboard · Simulaciones"` with a trailing chevron icon (`Icons.Rounded.ExpandMore`).
- **On Tap**: Smoothly expands (`AnimatedVisibility`) to reveal the existing SPEC03 and SPEC04 simulation buttons.
- **Expanded State**: The chevron rotates to `Icons.Rounded.ExpandLess`.

### 5.2 State Management

A local `var isDebugExpanded by remember { mutableStateOf(false) }` inside `MainDashboardScreen` controls the toggle. No ViewModel involvement needed — this is a purely presentational concern.

### 5.3 Animation

Use `AnimatedVisibility` with `expandVertically()` / `shrinkVertically()` and `tween(300)` for a smooth, premium feel.

---

## 6. Technical Constraints

1. **No New Sections**: The AI Copilot Card replaces `DashboardHeader`. The total number of top-level `item {}` blocks in the `LazyColumn` must not increase.
2. **Derived State**: Use `derivedStateOf` for `coachingMessage`, `completionProgress`, and `completionLabel` to avoid redundant recompositions.
3. **Streak Hardcode**: The streak counter is a simple `val` in the ViewModel. No persistence logic.
4. **String Templates**: Dynamic messages must use Kotlin string interpolation referencing actual habit block names and times from the list — not generic placeholder text.

---

## 7. Acceptance Criteria

| #  | Criterion                                                                                                  | Verification     |
| :- | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| 1  | The AI Copilot Card displays a progress bar reflecting the correct completion ratio.                       | UI Test          |
| 2  | Completing a habit updates the progress bar and coaching message instantly.                                 | UI Test          |
| 3  | Triggering SPEC03 (Friction) changes the coaching message to the friction-specific text.                   | UI Test          |
| 4  | Triggering SPEC04 (MCP Collision) changes the coaching message to the reallocation-specific text.          | UI Test          |
| 5  | The Debug Dashboard starts collapsed and expands smoothly on tap.                                          | UI Test          |
| 6  | The total number of visible sections on the dashboard does not increase compared to the current state.     | Visual Review    |
