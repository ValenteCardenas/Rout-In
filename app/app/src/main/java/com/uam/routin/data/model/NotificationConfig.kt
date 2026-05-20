package com.uam.routin.data.model

object NotificationConfig {
    // CANONICAL channel ID — must match NotificationHelper.CHANNEL_ID in Native_API_Recipes_Context.md §2
    const val CHANNEL_ID = "rout_in_behavioral_alerts"
    const val CHANNEL_NAME = "Behavioral Interventions"
    const val CHANNEL_DESC = "Contextual suggestions and MCP schedule synchronization alerts."

    // Notification IDs
    const val NOTIFICATION_ID_FRICTION  = 1001   // SPEC03: Proactive suggestion
    const val NOTIFICATION_ID_MCP       = 1002   // SPEC04: External calendar collision

    // Haptic pattern: { Delay, ON, OFF, ON, OFF, ON } in milliseconds
    val HAPTIC_PATTERN = longArrayOf(0, 250, 200, 250, 150, 400)
    val HAPTIC_AMPLITUDES = intArrayOf(0, 255, 0, 180, 0, 255) // API 26+

    // BroadcastReceiver action strings
    const val ACTION_MOVE_HABIT_630  = "com.uam.routin.ACTION_MOVE_HABIT_630"   // SPEC03
    const val ACTION_RELOCATE_730   = "com.uam.routin.ACTION_RELOCATE_HABIT_730"      // SPEC04

    // Intent extras
    const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    const val EXTRA_HABIT_ID        = "EXTRA_HABIT_ID"
}
