package com.uam.routin.viewmodel

/**
 * Unified sealed class representing voice processing and feedback states.
 * Shared across SPEC02, SPEC03, and SPEC04 to drive state animations in Compose.
 */
sealed class UiState {
    object Idle : UiState()
    object Listening : UiState()    // Microphone FAB held: wave animation active
    object Loading : UiState()      // FAB released: simulating cloud inference latency
    object Speaking : UiState()     // TTS engine is vocalizing the response
    data class Success(val message: String) : UiState()
    data class Error(val error: String) : UiState()
}
