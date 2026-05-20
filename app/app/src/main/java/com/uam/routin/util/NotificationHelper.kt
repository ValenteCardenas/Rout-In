package com.uam.routin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.uam.routin.data.model.NotificationConfig

/**
 * Singleton helper that bootstraps the application's high-importance notification channel.
 * Must be called from MainActivity.onCreate() before Compose content is mounted.
 * This is the canonical channel definition for SPEC01–SPEC04.
 */
object NotificationHelper {

    /** Canonical channel ID used across the entire application. Do NOT use any other string. */
    const val CHANNEL_ID = NotificationConfig.CHANNEL_ID

    private const val CHANNEL_NAME = NotificationConfig.CHANNEL_NAME
    private const val CHANNEL_DESC = NotificationConfig.CHANNEL_DESC

    /** Notification IDs for each simulation scenario */
    const val NOTIF_ID_FRICTION = NotificationConfig.NOTIFICATION_ID_FRICTION
    const val NOTIF_ID_MCP = NotificationConfig.NOTIFICATION_ID_MCP

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
