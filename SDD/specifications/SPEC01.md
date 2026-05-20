# SPEC01: Onboarding and Deployment Environment Selection

## 1. Traceability

| Attribute             | Value                                                                                            |
| :-------------------- | :----------------------------------------------------------------------------------------------- |
| **Source Feature**    | `features/OnboardingDeployment.feature`                                                          |
| **Architectural Ref** | `Architectural_Context.md §2` — Production vs. MVP Architecture Mapping                         |
| **Data Ref**          | `Mock_Data_Seed_Context.md §1` — Initial State Seed                                             |
| **UI Ref**            | `UI_Guide_Context.md §2, §3, §4` — Color palette, typography, component rules                  |
| **Tech Ref**          | `Tech-Stack_Context.md §1, §2, §3` — Build environment, Compose BOM, ViewModel                 |
| **Priority**          | **P0 — Blocker** (all other specs depend on this flow)                                           |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                                                |

---

## 2. Objective

Define the first-run (onboarding) flow where the user selects their architectural deployment mode: **Open-Source Self-Hosted** or **Premium Cloud Subscription**. This flow determines the system's communication configuration and is a prerequisite for all other features. It is the entry point for initializing the in-memory data store and setting up the `NotificationChannel` required by SPEC03 and SPEC04.

---

## 3. Preconditions

| #  | Precondition                                                        |
| :- | :------------------------------------------------------------------ |
| 1  | The application is being launched for the first time on the device. |
| 2  | The user is on the Identity Welcome Screen.                         |
| 3  | No previous configuration exists in the device preferences.         |

---

## 4. MVP Simulation Strategy

Per `Architectural_Context.md §2`, real Supabase Auth and cloud connections are **prohibited** in the MVP. The simulation strategy is:

| Production Component         | MVP Simulation                                                                                          |
| :--------------------------- | :------------------------------------------------------------------------------------------------------ |
| Supabase Auth                | Simulated "Authenticate" button that transitions directly to `UiState.Authenticated`.                  |
| MCP Client (Self-Hosted)     | URI input stored as `MutableState<String>` inside the ViewModel.                                        |
| Cloud Gateway SSE            | Direct transition to `UiState.CloudReady` state without a real connection.                              |
| OAuth 2.0 External Accounts  | Mock screen displaying providers (Google, Outlook) with simulation checkboxes.                          |
| Room / SharedPreferences     | All state persisted exclusively in `MutableState` inside the `ViewModel` — no disk I/O.                |

---

## 5. Data Model

> **Canonical source:** `Mock_Data_Seed_Context.md §1` for `HabitBlock`. The models below are onboarding-specific and must coexist in the same `ViewModel` as `HabitBlock`.

```kotlin
// file: data/model/OnboardingModels.kt

enum class DeploymentMode {
    OPEN_SOURCE_SELF_HOSTED,
    PREMIUM_CLOUD_SUBSCRIPTION
}

data class OnboardingState(
    val isAuthenticated: Boolean = false,
    val selectedMode: DeploymentMode? = null,
    val mcpServerUri: String = "",          // Only populated in OPEN_SOURCE_SELF_HOSTED mode
    val isOnboardingComplete: Boolean = false
)
```

**Canonical `HabitBlock` definition** (shared across SPEC01–SPEC04, authoritative source: `Mock_Data_Seed_Context.md §1`):

```kotlin
// file: data/model/HabitBlock.kt
data class HabitBlock(
    val id: Int,
    val name: String,
    val scheduledTime: String,   // "HH:mm" format — e.g., "14:00"
    val durationMinutes: Int,
    var status: String,          // "PENDING" | "COMPLETED" | "FRICTION" | "REALLOCATED" | "PENDING_REALLOCATION"
    var isImmutable: Boolean = false,
    var source: String = "INTERNAL" // "INTERNAL" | "EXTERNAL"
)
```

> ⚠️ **Constraint:** `HabitBlock.status` is a `String` constant (not an enum) for simplicity of in-memory mutation. All SPECs must reference the canonical string literals defined here.

**Canonical status literals:**

| Literal                | Used In               | Meaning                                              |
| :--------------------- | :-------------------- | :--------------------------------------------------- |
| `"PENDING"`            | SPEC01, 02, 03, 04    | Default state; habit scheduled but not yet started.  |
| `"COMPLETED"`          | SPEC01, 02            | Habit successfully executed.                         |
| `"FRICTION"`           | SPEC03                | Habit has been skipped 3+ consecutive times.         |
| `"REALLOCATED"`        | SPEC03                | Habit rescheduled by the system post-intervention.   |
| `"PENDING_REALLOCATION"`| SPEC04               | Habit displaced by an external immutable event.      |

---

## 6. State Machine Specification

```
[MainActivity.onCreate()]
    │
    ├─► NotificationHelper.createNotificationChannel(context)
    │   (Creates "rout_in_behavioral_alerts" channel — required by SPEC03, SPEC04)
    │
    └─► Mount Compose UI: WelcomeScreen

[WelcomeScreen]
    │
    ├─ User clicks "Authenticate"
    │     └─► onboardingState = onboardingState.copy(isAuthenticated = true)
    │         └─► UiState.Authenticated → show DeploymentSelectionScreen
    │
    ├─ User selects "Open-Source Community (Self-Hosted)"
    │     └─► onboardingState = onboardingState.copy(selectedMode = OPEN_SOURCE_SELF_HOSTED)
    │         └─► Navigate to LocalNetworkConfigScreen
    │               ├─► User inputs Wi-Fi MCP Server URI
    │               ├─► User taps "Save"
    │               ├─► onboardingState = onboardingState.copy(mcpServerUri = input, isOnboardingComplete = true)
    │               └─► Navigate to MainDashboardScreen
    │
    └─ User selects "Premium Cloud Subscription (Out-of-the-Box)"
          └─► onboardingState = onboardingState.copy(selectedMode = PREMIUM_CLOUD_SUBSCRIPTION)
              └─► Navigate to CloudOAuthMockScreen
                    ├─► Mock OAuth provider list (Google, Outlook) rendered with checkboxes
                    ├─► User taps "Continue"
                    ├─► onboardingState = onboardingState.copy(isOnboardingComplete = true)
                    └─► Navigate to MainDashboardScreen
```

---

## 7. UI Components Required

All components must implement the design tokens from `UI_Guide_Context.md §2–§4`:
- Background: `RoutInColors.DeepPurpleNavy`
- Card surface: `RoutInColors.DarkSurface`
- Primary text: `RoutInColors.OffWhiteSerenity`
- Accent / CTA: `RoutInColors.VibrantGreenEmphasis`
- Card shape: `RoundedCornerShape(24.dp)`
- Typography: `ComfortaaFontFamily` (Bold weight by default)

| Component ID               | Type                   | Unique ID (for UI testing)    | Description                                                                    |
| :------------------------- | :--------------------- | :---------------------------- | :----------------------------------------------------------------------------- |
| `WelcomeScreen`            | `@Composable` Screen   | `screen_welcome`              | Welcome screen with Rout-In branding and simulated "Authenticate" button.      |
| `AuthenticateButton`       | `@Composable` Button   | `btn_authenticate`            | `VibrantGreenEmphasis` colored CTA; triggers `isAuthenticated = true`.         |
| `DeploymentSelectionScreen`| `@Composable` Screen   | `screen_deployment_selection` | Hosts two `DeploymentSelectionCard` items side by side.                        |
| `DeploymentSelectionCard`  | `@Composable` Card     | `card_self_hosted` / `card_cloud` | Material 3 card (`RoundedCornerShape(24.dp)`) with title, description, icon.  |
| `LocalNetworkConfigScreen` | `@Composable` Screen   | `screen_local_net_config`     | `OutlinedTextField` for MCP server URI + "Save" button.                        |
| `CloudOAuthMockScreen`     | `@Composable` Screen   | `screen_cloud_oauth`          | List of simulated OAuth providers (Google, Outlook) with checkboxes + continue.|

---

## 8. Technical Constraints

1. **No External Libraries**: Per `Tech-Stack_Context.md §5`, Supabase SDK and Firebase Auth are not permitted. Authentication is simulated locally.
2. **State Isolation**: Per `Architectural_Context.md §4.1`, all business logic resides in `RoutInViewModel`. `@Composable` functions observe reactive states only via `StateFlow` / `MutableState`.
3. **In-Memory Persistence**: `OnboardingState` is stored as `MutableState` inside the ViewModel; no real `SharedPreferences` or disk writes.
4. **NotificationChannel Bootstrap**: `MainActivity.onCreate()` must call `NotificationHelper.createNotificationChannel(this)` before mounting Compose content. This is mandatory for SPEC03 and SPEC04 to function. See `Native_API_Recipes_Context.md §2` for the canonical channel definition.
5. **Canonical Channel ID**: The notification channel ID used across the entire application is **`"rout_in_behavioral_alerts"`** (defined in `Native_API_Recipes_Context.md §2`). Do not use `"rout_in_proactive"` or any other string.
6. **Compose BOM**: Use `Jetpack Compose BOM 2026.04.00` per `Tech-Stack_Context.md §2`.

---

## 9. Acceptance Criteria

| #  | Criterion                                                                                             | Verification        |
| :- | :---------------------------------------------------------------------------------------------------- | :------------------ |
| 1  | Pressing "Authenticate" transitions the state to `isAuthenticated = true` without network calls.      | Unit Test           |
| 2  | Selecting "Self-Hosted" navigates to the local network configuration screen.                          | UI Test             |
| 3  | Saving a valid URI stores it in `OnboardingState.mcpServerUri` and sets `isOnboardingComplete = true`.| Unit Test           |
| 4  | Selecting "Premium Cloud" displays the mock OAuth screen and sets `isOnboardingComplete = true`.      | UI Test             |
| 5  | `MainActivity.onCreate()` creates the `"rout_in_behavioral_alerts"` `NotificationChannel`.            | Device Test         |
| 6  | No external authentication or networking dependencies are imported.                                   | Build Verification  |
| 7  | All UI components use `RoutInColors.*` palette and `ComfortaaFontFamily` typeface.                    | UI Test / Code Review|
| 8  | All buttons and cards have the unique test IDs specified in §7.                                       | UI Test             |
