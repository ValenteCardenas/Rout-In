# SPEC01: Onboarding and Deployment Environment Selection

## 1. Traceability

| Attribute             | Value                                                              |
| :-------------------- | :----------------------------------------------------------------- |
| **Source Feature**    | `features/OnboardingDeployment.feature`                            |
| **Architectural Ref** | `Architectural_Context.md §2` — Production vs. MVP Architecture Mapping |
| **Priority**          | **P0 — Blocker** (all other specs depend on this flow)             |
| **Target Platform**   | Android (minSdk 26, targetSdk 35)                                  |

---

## 2. Objective

Define the first-run (onboarding) flow where the user selects their architectural deployment mode: **Open-Source Self-Hosted** or **Premium Cloud Subscription**. This flow determines the system's communication configuration and is a prerequisite for all other features.

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

| Production Component           | MVP Simulation                                                                      |
| :----------------------------- | :---------------------------------------------------------------------------------- |
| Supabase Auth                  | Simulated "Authenticate" button that transitions directly to `UiState.Authenticated`. |
| MCP Client (Self-Hosted)       | URI input stored as `MutableState<String>` inside the ViewModel.                     |
| Cloud Gateway SSE              | Direct transition to `UiState.CloudReady` state without a real connection.            |
| OAuth 2.0 External Accounts   | Mock screen displaying providers (Google, Outlook) with simulation checkboxes.        |

---

## 5. Data Model

```kotlin
enum class DeploymentMode {
    OPEN_SOURCE_SELF_HOSTED,
    PREMIUM_CLOUD_SUBSCRIPTION
}

data class OnboardingState(
    val isAuthenticated: Boolean = false,
    val selectedMode: DeploymentMode? = null,
    val mcpServerUri: String = "",         // Self-Hosted only
    val isOnboardingComplete: Boolean = false
)
```

---

## 6. State Machine Specification

```
[WelcomeScreen]
    │
    ├─ User clicks "Authenticate" ──► UiState.Authenticated
    │
    ├─ User selects "Open-Source Community (Self-Hosted)"
    │     │
    │     ├─► Navigate to LocalNetworkConfigScreen
    │     ├─► User inputs Wi-Fi MCP Server URI
    │     ├─► User saves a valid URI
    │     ├─► Store config in device preferences (simulated as in-memory MutableState)
    │     └─► Initialize Kotlin MCP SDK client target (simulated: set mcpServerUri state)
    │         └─► UiState.OnboardingComplete
    │
    └─ User selects "Premium Cloud Subscription (Out-of-the-Box)"
          │
          ├─► Route to managed cloud gateway via SSE (simulated: direct state transition)
          ├─► Display automated OAuth 2.0 interface (mock UI with provider list)
          └─► UiState.OnboardingComplete
```

---

## 7. UI Components Required

| Component ID              | Type                   | Description                                                        |
| :------------------------ | :--------------------- | :----------------------------------------------------------------- |
| `WelcomeScreen`           | `@Composable` Screen   | Welcome screen with Rout-In branding and simulated authentication button. |
| `DeploymentSelectionCard` | `@Composable` Card     | Material 3 card with title, description, and icon for each deployment mode. |
| `LocalNetworkConfigScreen`| `@Composable` Screen   | Form with `OutlinedTextField` for local MCP server URI + save button. |
| `CloudOAuthMockScreen`    | `@Composable` Screen   | List of simulated OAuth providers (Google, Outlook) with checkboxes and continue button. |

---

## 8. Technical Constraints

1. **No External Libraries**: Per `Tech-Stack_Context.md §5`, Supabase SDK and Firebase Auth are not permitted. Authentication is simulated locally.
2. **State Isolation**: Per `Architectural_Context.md §4.1`, all business logic resides in the ViewModel. `@Composable` functions only observe reactive states.
3. **In-Memory Persistence**: Onboarding configuration is stored as `MutableState` inside the ViewModel, not in real SharedPreferences.

---

## 9. Acceptance Criteria

| #  | Criterion                                                                                           | Verification        |
| :- | :-------------------------------------------------------------------------------------------------- | :------------------ |
| 1  | Pressing "Authenticate" transitions the state to `Authenticated` without network calls.             | Unit Test / UI Test |
| 2  | Selecting "Self-Hosted" correctly navigates to the local network configuration screen.               | UI Test             |
| 3  | Saving a valid URI stores the configuration in the ViewModel state.                                  | Unit Test           |
| 4  | Selecting "Premium Cloud" displays the mock OAuth interface and transitions to `OnboardingComplete`. | UI Test             |
| 5  | No external authentication or networking dependencies are imported.                                  | Build Verification  |
