package com.uam.routin.data.model

/**
 * Represents the architectural deployment mode chosen by the user during onboarding.
 * This token is stored in-memory inside the ViewModel — no disk persistence.
 */
enum class DeploymentMode {
    OPEN_SOURCE_SELF_HOSTED,
    PREMIUM_CLOUD_SUBSCRIPTION
}

/**
 * Immutable snapshot of the onboarding state machine.
 * All fields default to their pre-authentication values.
 * Mutations are performed via ViewModel.copy() to trigger Compose recomposition.
 */
data class OnboardingState(
    val isAuthenticated: Boolean = false,
    val selectedMode: DeploymentMode? = null,
    val mcpServerUri: String = "",           // Only populated in OPEN_SOURCE_SELF_HOSTED mode
    val isOnboardingComplete: Boolean = false
)
