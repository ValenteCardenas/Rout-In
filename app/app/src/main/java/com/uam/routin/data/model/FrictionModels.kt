package com.uam.routin.data.model

data class FrictionMetrics(
    val habitId: Int,                               // Matches HabitBlock.id
    val consecutiveSkips: Int = 0,
    val isCritical: Boolean = consecutiveSkips >= 3
)
