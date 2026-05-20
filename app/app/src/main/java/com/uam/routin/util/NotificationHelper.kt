package com.uam.routin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    /** SPEC11: Dispatches the real-time alarm notification for a scheduled habit */
    fun dispatchRoutineAlarmNotification(context: Context, habitName: String, habitId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("¡Es hora de tu rutina!")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(NotificationConfig.HAPTIC_PATTERN)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(habitId, builder.build())
            }
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace()
        }
    }
}
