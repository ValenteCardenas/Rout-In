package com.uam.routin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Singleton helper that bootstraps the application's high-importance notification channel.
 * Must be called from MainActivity.onCreate() before Compose content is mounted.
 * This is the canonical channel definition for SPEC01–SPEC04.
 */
object NotificationHelper {

    /** Canonical channel ID used across the entire application. Do NOT use any other string. */
    const val CHANNEL_ID = "rout_in_behavioral_alerts"

    private const val CHANNEL_NAME = "Behavioral Interventions"
    private const val CHANNEL_DESC =
        "Contextual suggestions and MCP schedule synchronization alerts."

    /** Notification IDs for each simulation scenario */
    const val NOTIF_ID_FRICTION = 1001
    const val NOTIF_ID_MCP = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
