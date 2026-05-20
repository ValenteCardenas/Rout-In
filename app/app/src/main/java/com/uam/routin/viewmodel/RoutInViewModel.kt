package com.uam.routin.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uam.routin.data.model.DeploymentMode
import com.uam.routin.data.model.HabitBlock
import com.uam.routin.data.model.MockDataSeed
import com.uam.routin.data.model.OnboardingState
import com.uam.routin.receiver.NotificationActionReceiver
import com.uam.routin.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** Represents the voice interaction processing state for the FAB wave animation. */
enum class VoiceUiState { IDLE, PROCESSING }

/**
 * Central In-Memory State Machine for the Rout-In MVP.
 *
 * Responsibilities:
 * - Holds all reactive state streams observed by the Compose UI layer.
 * - Drives all four autonomous simulation flows (SPEC01–SPEC04).
 * - Manages lifecycle-aware TextToSpeech resources.
 * - Registers callback hooks for the NotificationActionReceiver.
 *
 * Architecture constraint: NO business logic in @Composable functions.
 * All mutations happen here; the UI observes state via [State] delegates.
 */
class RoutInViewModel(application: Application) :
    AndroidViewModel(application), TextToSpeech.OnInitListener {

    // ─── Reactive State Streams ───────────────────────────────────────────────

    private val _onboardingState = mutableStateOf(OnboardingState())
    val onboardingState: State<OnboardingState> = _onboardingState

    private val _habitBlocks = mutableStateOf<List<HabitBlock>>(MockDataSeed.getInitialHabits())
    val habitBlocks: State<List<HabitBlock>> = _habitBlocks

    private val _voiceUiState = mutableStateOf(VoiceUiState.IDLE)
    val voiceUiState: State<VoiceUiState> = _voiceUiState

    // ─── Native Hardware Engines ──────────────────────────────────────────────

    private var ttsEngine: TextToSpeech? = TextToSpeech(application, this)
    private var isTtsInitialized = false

    // ─── Initialization ───────────────────────────────────────────────────────

    init {
        // Wire notification action callbacks so the receiver can dispatch UI mutations
        NotificationActionReceiver.onMoveHabit630 = { onNotificationMoveReadingTo1830() }
        NotificationActionReceiver.onRelocateHabit730 = { onNotificationRelocateGymTo1930() }
    }

    // ─── TextToSpeech Lifecycle ───────────────────────────────────────────────

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = ttsEngine?.setLanguage(Locale.forLanguageTag("es-MX"))
            isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speakEmpatheticResponse(text: String) {
        if (isTtsInitialized) {
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RoutInVoiceTriggerID")
        }
    }

    // ─── SPEC01: Onboarding State Mutations ───────────────────────────────────

    /** Simulates authentication — no network call involved. */
    fun onAuthenticate() {
        _onboardingState.value = _onboardingState.value.copy(isAuthenticated = true)
    }

    /** Records the user's chosen deployment mode and triggers navigation in the UI. */
    fun onSelectDeploymentMode(mode: DeploymentMode) {
        _onboardingState.value = _onboardingState.value.copy(selectedMode = mode)
    }

    /** Stores the MCP server URI and marks onboarding as complete (Self-Hosted path). */
    fun onSaveMcpUri(uri: String) {
        _onboardingState.value = _onboardingState.value.copy(
            mcpServerUri = uri,
            isOnboardingComplete = true
        )
    }

    /** Marks onboarding complete after mock OAuth provider selection (Cloud path). */
    fun onCloudOAuthContinue() {
        _onboardingState.value = _onboardingState.value.copy(isOnboardingComplete = true)
    }

    // ─── SPEC02: Voice-Driven Schedule Modification ───────────────────────────

    /**
     * Simulates the full voice command flow:
     * 1. Enters PROCESSING state (wave animation in UI)
     * 2. Delays 1500ms to emulate cloud inference latency
     * 3. Speaks empathetic TTS response
     * 4. Injects "Junta de Proyecto de Investigación" at 17:00
     * 5. Shifts Gym / Workout from 18:00 → 19:00
     */
    fun simulateVoiceCommand() {
        viewModelScope.launch {
            _voiceUiState.value = VoiceUiState.PROCESSING

            withContext(Dispatchers.Default) { delay(1_500L) }

            speakEmpatheticResponse(
                "Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de " +
                        "Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."
            )

            mutateHabitBlocks {
                // Shift all internal non-immutable habits at or after 17:00 by 60 minutes
                forEach { block ->
                    if (!block.isImmutable
                        && block.source == HabitBlock.Source.INTERNAL
                        && block.scheduledTime >= "17:00"
                    ) {
                        block.scheduledTime = shiftTime(block.scheduledTime, 60)
                    }
                }
                // Insert the new immutable meeting block at 17:00
                add(
                    HabitBlock(
                        id = 201,
                        name = "Junta de Proyecto de Investigación",
                        scheduledTime = "17:00",
                        durationMinutes = 60,
                        status = HabitBlock.StatusConstants.PENDING,
                        isImmutable = true,
                        source = HabitBlock.Source.INTERNAL
                    )
                )
                sortBy { it.scheduledTime }
            }

            _voiceUiState.value = VoiceUiState.IDLE
        }
    }

    // ─── SPEC03: Proactive Behavioral Suggestion (Critical Friction) ──────────

    /**
     * Simulates critical friction detection on the Reading Block:
     * 1. Marks Reading Block as FRICTION
     * 2. Triggers haptic wave pattern
     * 3. Fires high-priority interactive push notification
     */
    fun simulateCriticalFriction() {
        val context = getApplication<Application>()

        // Step 1 — Mutate Reading Block status to FRICTION
        mutateHabitBlocks {
            find { it.id == 102 }?.status = HabitBlock.StatusConstants.FRICTION
        }

        // Step 2 — Trigger sensory-disruptive haptic vibration
        triggerSensoryVibration(context)

        // Step 3 — Fire interactive push notification
        dispatchFrictionNotification(context)
    }

    /** Called by NotificationActionReceiver when "Move to 6:30 PM" is tapped. */
    fun onNotificationMoveReadingTo1830() {
        mutateHabitBlocks {
            find { it.id == 102 }?.apply {
                scheduledTime = "18:30"
                status = HabitBlock.StatusConstants.REALLOCATED
            }
            sortBy { it.scheduledTime }
        }
    }

    // ─── SPEC04: External MCP Calendar Reconciliation ─────────────────────────

    /**
     * Simulates ingestion of a mock MCP calendar payload:
     * 1. Injects "Sistemas Operativos Exam" at 18:00 (immutable, external)
     * 2. Sets Gym / Workout to PENDING_REALLOCATION
     * 3. Fires empathetic rescheduling notification
     */
    fun simulateMcpCollision() {
        val context = getApplication<Application>()

        mutateHabitBlocks {
            // Displace Gym / Workout — it collides with the exam
            find { it.id == 103 }?.status = HabitBlock.StatusConstants.PENDING_REALLOCATION

            // Inject the external immutable exam block
            add(
                HabitBlock(
                    id = 301,
                    name = "Sistemas Operativos Exam",
                    scheduledTime = "18:00",
                    durationMinutes = 120,
                    status = HabitBlock.StatusConstants.PENDING,
                    isImmutable = true,
                    source = HabitBlock.Source.EXTERNAL
                )
            )
            sortBy { it.scheduledTime }
        }

        dispatchMcpCollisionNotification(context)
    }

    /** Called by NotificationActionReceiver when "Re-locate to 7:30 PM" is tapped. */
    fun onNotificationRelocateGymTo1930() {
        mutateHabitBlocks {
            find { it.id == 103 }?.apply {
                scheduledTime = "19:30"
                status = HabitBlock.StatusConstants.PENDING
            }
            sortBy { it.scheduledTime }
        }
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Thread-safe helper for mutating the habit list.
     * Always creates a new list reference to guarantee Compose recomposition.
     */
    private fun mutateHabitBlocks(transform: MutableList<HabitBlock>.() -> Unit) {
        val mutable = _habitBlocks.value.toMutableList()
        mutable.transform()
        _habitBlocks.value = mutable
    }

    /**
     * Shifts a "HH:mm" time string forward by [minutesToAdd] minutes.
     * Handles hour overflow correctly within a 24-hour clock.
     */
    private fun shiftTime(time: String, minutesToAdd: Int): String {
        val parts = time.split(":")
        val totalMinutes = parts[0].toInt() * 60 + parts[1].toInt() + minutesToAdd
        val newHour = (totalMinutes / 60) % 24
        val newMinutes = totalMinutes % 60
        return "%02d:%02d".format(newHour, newMinutes)
    }

    /** Emits the irregular haptic burst pattern to penetrate notification blindness. */
    private fun triggerSensoryVibration(context: Context) {
        val timings = longArrayOf(0, 250, 200, 250, 150, 400)
        val amplitudes = intArrayOf(0, 255, 0, 180, 0, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            manager.defaultVibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        }
    }

    /** Builds and fires the SPEC03 interactive friction notification. */
    @Suppress("MissingPermission")
    private fun dispatchFrictionNotification(context: Context) {
        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MOVE_HABIT_630
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, NotificationHelper.NOTIF_ID_FRICTION)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, pendingFlag)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rout-In · Intervención Proactiva")
            .setContentText(
                "Hola. Notamos que este horario te ha costado trabajo. " +
                        "¿Prefieres mover la lectura a hoy a las 6:30 PM?"
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Hola. Notamos que este horario te ha costado trabajo últimamente. " +
                            "No te preocupes, vamos a tu ritmo. ¿Prefieres mover la lectura " +
                            "a hoy a las 6:30 PM o prefieres que lo intentemos el sábado por la mañana?"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_today, "Move to 6:30 PM", actionPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationHelper.NOTIF_ID_FRICTION, notification)
    }

    /** Builds and fires the SPEC04 MCP collision rescheduling notification. */
    @Suppress("MissingPermission")
    private fun dispatchMcpCollisionNotification(context: Context) {
        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_RELOCATE_HABIT_730
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, NotificationHelper.NOTIF_ID_MCP)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(context, 1, actionIntent, pendingFlag)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rout-In · Sincronización MCP")
            .setContentText(
                "Tu examen de Sistemas Operativos interfiere con tu gimnasio. " +
                        "¿Reubicamos tu rutina a las 7:30 PM?"
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Tu examen de Sistemas Operativos se movió a las 6:00 PM e interfiere " +
                            "con tu gimnasio. Hemos protegido tu bloque académico para que te enfoques. " +
                            "¿Deseas reubicar tu rutina de entrenamiento hoy a las 7:30 PM?"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_today, "Re-locate to 7:30 PM", actionPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationHelper.NOTIF_ID_MCP, notification)
    }

    // ─── Lifecycle Cleanup ────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Release TTS hardware resources to prevent memory leaks on device
        ttsEngine?.stop()
        ttsEngine?.shutdown()
        ttsEngine = null
        // Clear receiver callbacks to prevent stale references
        NotificationActionReceiver.onMoveHabit630 = null
        NotificationActionReceiver.onRelocateHabit730 = null
    }
}
