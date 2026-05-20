package com.uam.routin.data.model

/**
 * Canonical data model representing a single scheduled routine block.
 * This is the authoritative definition shared across SPEC01–SPEC04.
 * Status literals are defined as companion object constants to enforce consistency.
 */
data class HabitBlock(
    val id: Int,
    val name: String,
    var scheduledTime: String,       // Format: "HH:mm" — e.g., "14:00"
    val durationMinutes: Int,
    var status: String,              // See StatusConstants below
    var isImmutable: Boolean = false,
    var source: String = Source.INTERNAL
) {
    /** Canonical status string literals used across all SPECs. */
    object StatusConstants {
        const val PENDING = "PENDING"
        const val COMPLETED = "COMPLETED"
        const val FRICTION = "FRICTION"
        const val REALLOCATED = "REALLOCATED"
        const val PENDING_REALLOCATION = "PENDING_REALLOCATION"
    }

    /** Source identifiers for habit blocks. */
    object Source {
        const val INTERNAL = "INTERNAL"  // Created by Rout-In
        const val EXTERNAL = "EXTERNAL"  // Injected via MCP / calendar sync
    }
}

/**
 * Seed object providing the default in-memory state for Gabriel's afternoon schedule.
 * Called once during ViewModel initialization. No disk I/O involved.
 */
object MockDataSeed {
    fun getInitialHabits(): MutableList<HabitBlock> = mutableListOf(
        HabitBlock(
            id = 101,
            name = "Clase de Arq. de Computadoras",
            scheduledTime = "12:00",
            durationMinutes = 120,
            status = HabitBlock.StatusConstants.COMPLETED,
            isImmutable = true,
            source = HabitBlock.Source.EXTERNAL
        ),
        HabitBlock(
            id = 102,
            name = "Almuerzo",
            scheduledTime = "14:00",
            durationMinutes = 60,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        ),
        HabitBlock(
            id = 103,
            name = "Estudio Cálculo",
            scheduledTime = "15:00",
            durationMinutes = 60,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        ),
        HabitBlock(
            id = 104,
            name = "Proyecto de Software",
            scheduledTime = "16:00",
            durationMinutes = 60,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        ),
        HabitBlock(
            id = 108,
            name = "Reading Block",
            scheduledTime = "17:00",
            durationMinutes = 20,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        ),
        HabitBlock(
            id = 106,
            name = "Gym / Ejercicio",
            scheduledTime = "18:00",
            durationMinutes = 90,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        ),
        HabitBlock(
            id = 107,
            name = "Cena",
            scheduledTime = "19:30",
            durationMinutes = 60,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        )
    )
}
