package com.uam.routin.data.model

data class ExternalEvent(
    val mcpEventId: String,         // e.g., "mcp_evt_os_992"
    val name: String,               // e.g., "Sistemas Operativos Exam"
    val scheduledTime: String,      // "HH:mm" — e.g., "18:00"
    val durationMinutes: Int,       // e.g., 120
    val isImmutable: Boolean = true,
    val source: String = "Google Calendar"
)
