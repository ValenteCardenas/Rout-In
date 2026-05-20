# Rout-In MVP: Native Android API Recipes & Simulation Code Snippets

This document provides modern, non-deprecated Kotlin recipes for interacting with native Android hardware and system services. The agéntic IDE (**Antigravity**) must use these snippets inside the `ViewModel` or local simulation triggers to drive the interactive presentation of the **Rout-In** MVP.

---

## 1. Lifecycle-Aware Text-to-Speech (TTS) Engine

This recipe handles local audio synthesis, allowing the `ViewModel` to vocally deliver the agent's empathetic response when a voice command is processed.

### Implementation Snippet:
```kotlin
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import java.util.Locale

class RoutInViewModel(context: Context) : ViewModel(), TextToSpeech.OnInitListener {

    private var ttsEngine: TextToSpeech? = TextToSpeech(context, this)
    private var isTtsInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = ttsEngine?.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
            }
        }
    }

    fun speakEmpatheticResponse(text: String) {
        if (isTtsInitialized) {
            ttsEngine?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "RoutInVoiceTriggerID"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Critical constraint: release hardware resources to avoid memory leaks
        ttsEngine?.stop()
        ttsEngine?.shutdown()
    }
}

```

---

## 2. High-Importance Notification Channel Creation

Android 8.0 (API 26) and above requires channels to display alerts. This recipe sets up a high-importance channel so that interactive suggestions pop up as an emergency visual banner (*Heads-up notification*).

### Implementation Snippet:

```kotlin
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "rout_in_behavioral_alerts"
    private const val CHANNEL_NAME = "Behavioral Interventions"
    private const val CHANNEL_DESC = "Contextual suggestions and MCP schedule synchronization alerts."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

```

---

## 3. Interactive Push Notification with One-Tap Action Buttons

This recipe builds and broadcasts the physical push notification layout. It incorporates interactive option buttons powered by a `PendingIntent` to capture user redirection requests.

### Implementation Snippet:

```kotlin
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// 1. Receiver to capture the notification button tap
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == "ACTION_MOVE_HABIT_630") {
            // Dismiss the notification tray entry
            val notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)
            context?.let {
                val manager = it.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.cancel(notificationId)
                
                // ROUTE SIGNAL: Broadcast back to the ViewModel state holder to shift the UI state
                val uiSyncIntent = Intent("UPDATE_UI_STATE_HABIT_SHIFT")
                it.sendBroadcast(uiSyncIntent)
            }
        }
    }
}

// 2. Function to dispatch the notification layout
fun dispatchInteractiveSuggestion(context: Context, notificationId: Int = 1001) {
    // Build the underlying action intent
    val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_MOVE_HABIT_630"
        putExtra("NOTIFICATION_ID", notificationId)
    }

    // Modern constraint: explicitly specify PendingIntent flag mapping requirement
    val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    val actionPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        actionIntent,
        pendingIntentFlag
    )

    val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Placeholder framework icon
        .setContentTitle("Rout-In Assistance")
        .setContentText("Notamos que este horario te cuesta trabajo. ¿Lo movemos a las 6:30 PM?")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .addAction(
            android.R.drawable.ic_menu_today,
            "Move to 6:30 PM",
            actionPendingIntent
        )

    with(NotificationManagerCompat.from(context)) {
        // Ensure POST_NOTIFICATIONS permission check is bypassed or handled locally in MainActivity
        notify(notificationId, builder.build())
    }
}

```

---

## 4. Sensory-Disruptive Haptic Vibration Wave

This snippet invokes the physical vibration hardware to produce an irregular haptic burst pattern designed to penetrate cognitive fatigue and mitigate notification blindness.

### Implementation Snippet:

```kotlin
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun triggerSensoryDisruptiveVibration(context: Context) {
    // Array specifying timing: { Delay, Vibrate, Pause, Vibrate, Pause, Vibrate }
    val timings = longArrayOf(0, 250, 200, 250, 150, 400)
    // Corresponding structural amplitude arrays (0 is off, 255 is maximum motor drive force)
    val amplitudes = intArrayOf(0, 255, 0, 180, 0, 255)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val combinedVibrator = vibratorManager.defaultVibrator
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1) // -1 ensures no looping
        combinedVibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        val legacyVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            legacyVibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            legacyVibrator.vibrate(timings, -1)
        }
    }
}

```
