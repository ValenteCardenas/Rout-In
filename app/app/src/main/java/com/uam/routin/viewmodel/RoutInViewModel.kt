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
import com.uam.routin.data.model.ExternalEvent
import com.uam.routin.data.model.HabitBlock
import com.uam.routin.data.model.MockDataSeed
import com.uam.routin.data.model.NotificationConfig
import com.uam.routin.data.model.OnboardingState
import com.uam.routin.receiver.NotificationActionReceiver
import com.uam.routin.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Refactored to use the unified sealed class UiState

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

    private val _habitBlocks = mutableStateOf<List<HabitBlock>>(
        MockDataSeed.getInitialHabits().toMutableList().also { resolveCollisions(it) }
    )
    val habitBlocks: State<List<HabitBlock>> = _habitBlocks

    private val _uiState = mutableStateOf<UiState>(UiState.Idle)
    val uiState: State<UiState> = _uiState

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
            var result = ttsEngine?.setLanguage(Locale.forLanguageTag("es-MX"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to device default locale if es-MX is not installed on this device/emulator
                result = ttsEngine?.setLanguage(Locale.getDefault())
            }
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

    /** Triggers when the user physically presses down the microphone. */
    fun onMicPressStart() {
        // Only start if we are in an idle or success state
        if (_uiState.value != UiState.Idle && _uiState.value !is UiState.Success) return
        _uiState.value = UiState.Listening
    }

    /** Triggers when the user releases the microphone. */
    fun onMicRelease() {
        // Guard to only process if we were actually listening
        if (_uiState.value != UiState.Listening) return

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            // Simulate STT/NLP cloud inference latency after they finish speaking
            withContext(Dispatchers.Default) {
                delay(1500L)
            }

            // Speaking and list mutation
            withContext(Dispatchers.Main) {
                _uiState.value = UiState.Speaking
                speakEmpatheticResponse(
                    "Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de " +
                            "Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."
                )

                mutateHabitBlocks {
                    // Shift all non-immutable blocks scheduled at or after 17:00 forward by 60 minutes
                    forEach { block ->
                        if (block.scheduledTime >= "17:00" && !block.isImmutable) {
                            block.scheduledTime = shiftTime(block.scheduledTime, 60)
                        }
                    }
                    // Remove old meeting if it exists to allow re-running the demo seamlessly
                    removeAll { it.id == 201 }
                    // Insert new immutable meeting block at 17:00 (SPEC02 §6)
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

                // Keep speaking state visible for a bit to read the subtitle
                delay(4000L)
                _uiState.value = UiState.Success("Schedule updated.")
                delay(3000L)
                _uiState.value = UiState.Idle
            }
        }
    }

    /** No-op fallback */
    fun simulateVoiceCommand() {
        // Handled automatically by interaction source now
    }

    // ─── SPEC03: Proactive Behavioral Suggestion (Critical Friction) ──────────

    /**
     * Simulates critical friction detection on the Reading Block:
     * 1. Restores Reading Block to 17:00 PENDING (Precondition Reset per SPEC03 §9.9)
     * 2. Marks Reading Block as FRICTION
     * 3. Triggers haptic wave pattern
     * 4. Fires high-priority interactive push notification
     */
    fun simulateCriticalFriction() {
        val context = getApplication<Application>()

        // Step 1: Precondition Reset (Order-Independence per SPEC03 §9.9)
        mutateHabitBlocks {
            find { it.id == 108 }?.apply {
                scheduledTime = "17:00"
                status = HabitBlock.StatusConstants.PENDING
            }
            sortBy { it.scheduledTime }
        }

        // Step 2: Mutate Reading Block (id=108) status to FRICTION
        mutateHabitBlocks {
            find { it.id == 108 }?.status = HabitBlock.StatusConstants.FRICTION
        }

        // Step 3 — Trigger sensory-disruptive haptic vibration
        triggerSensoryVibration(context)

        // Step 4 — Fire interactive push notification
        dispatchFrictionNotification(context)
    }

    /** Called by NotificationActionReceiver when "Move 30 mins later" is tapped. */
    fun onNotificationMoveReadingTo1830() {
        mutateHabitBlocks {
            find { it.id == 108 }?.apply {
                scheduledTime = shiftTime(scheduledTime, 30)
                status = HabitBlock.StatusConstants.REALLOCATED
            }
            sortBy { it.scheduledTime }
        }
    }

    // ─── SPEC04: External MCP Calendar Reconciliation ─────────────────────────

    /**
     * Simulates ingestion of a mock MCP calendar payload:
     * 1. Restores Gym block to 18:00 PENDING and removes old exams (Precondition Reset per SPEC04 §9.9)
     * 2. Executes the Calendar Priority Rule: Gym (id=106) -> PENDING_REALLOCATION
     * 3. Injects "Sistemas Operativos Exam" at 18:00 (immutable, external)
     * 4. Fires empathetic rescheduling notification
     */
    fun simulateMcpCollision() {
        val context = getApplication<Application>()

        // Step 1: Precondition Reset (Order-Independence per SPEC04 §9.9)
        mutateHabitBlocks {
            removeAll { it.name == "Sistemas Operativos Exam" || it.id == "mcp_evt_os_992".hashCode() }
            find { it.id == 106 }?.apply {
                scheduledTime = "18:00"
                status = HabitBlock.StatusConstants.PENDING
                isImmutable = false
            }
            sortBy { it.scheduledTime }
        }

        // Step 2 & 3: Ingest external event and apply Priority Rule
        val mockMcpEvent = ExternalEvent(
            mcpEventId = "mcp_evt_os_992",
            name = "Sistemas Operativos Exam",
            scheduledTime = "18:00",
            durationMinutes = 120,
            isImmutable = true,
            source = "Google Calendar"
        )

        mutateHabitBlocks {
            applyCalendarPriorityRule(mockMcpEvent, this)
        }

        dispatchMcpCollisionNotification(context)
    }

    private fun applyCalendarPriorityRule(event: ExternalEvent, habits: MutableList<HabitBlock>) {
        // 1. Find any internal block colliding at event.scheduledTime
        val displaced = habits.find {
            it.scheduledTime == event.scheduledTime && !it.isImmutable
        }
        // 2. Displace the conflicting block
        displaced?.let { it.status = HabitBlock.StatusConstants.PENDING_REALLOCATION }
        // 3. Insert the external event as an immutable HabitBlock
        habits.add(
            HabitBlock(
                id = event.mcpEventId.hashCode(),
                name = event.name,
                scheduledTime = event.scheduledTime,
                durationMinutes = event.durationMinutes,
                status = HabitBlock.StatusConstants.PENDING,
                isImmutable = true,
                source = HabitBlock.Source.EXTERNAL
            )
        )
        habits.sortBy { it.scheduledTime }
    }

    /** Called by NotificationActionReceiver when "Re-locate to 7:30 PM" is tapped. */
    fun onNotificationRelocateGymTo1930() {
        mutateHabitBlocks {
            find { it.id == 106 }?.apply {
                scheduledTime = "19:30"
                status = HabitBlock.StatusConstants.PENDING
            }
            sortBy { it.scheduledTime }
        }
    }

    // ─── SPEC05: In-Memory Habit Creation ───────────────────────────────────────

    /**
     * Creates a new custom habit block and appends it to the in-memory list.
     * ID generation uses sequential integer logic to prevent Compose
     * duplicate key crashes in LazyColumn.
     *
     * @param name          Display name for the habit block
     * @param time          Scheduled time in "HH:mm" format
     * @param duration      Duration in minutes
     */
    fun addCustomHabit(name: String, time: String, duration: Int) {
        val newId = (_habitBlocks.value.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = HabitBlock(
            id = newId,
            name = name,
            scheduledTime = time,
            durationMinutes = duration,
            status = HabitBlock.StatusConstants.PENDING,
            isImmutable = false,
            source = HabitBlock.Source.INTERNAL
        )
        mutateHabitBlocks { add(newHabit) }
    }

    // ─── SPEC06: Interactive Habit Gamification and Completion ─────────────────

    /**
     * Toggles a habit block's status between COMPLETED and PENDING.
     * Uses immutable list mapping to guarantee Compose recomposition.
     * Only affects the target habit; all other blocks remain unchanged.
     *
     * @param habitId  The unique ID of the habit block to toggle
     */
    fun toggleHabitCompletion(habitId: Int) {
        val updatedList = _habitBlocks.value.map { block ->
            if (block.id == habitId) {
                val nextStatus = if (block.status == HabitBlock.StatusConstants.COMPLETED) {
                    HabitBlock.StatusConstants.PENDING
                } else {
                    HabitBlock.StatusConstants.COMPLETED
                }
                block.copy(status = nextStatus)
            } else {
                block
            }
        }
        _habitBlocks.value = updatedList
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Resolves schedule collisions by cascading flexible blocks forward.
     * Immutable blocks remain anchored.
     */
    private fun resolveCollisions(habits: MutableList<HabitBlock>) {
        habits.sortBy { timeToMinutes(it.scheduledTime) }
        var currentMinutes = 0

        for (i in habits.indices) {
            val block = habits[i]
            if (block.isImmutable) {
                currentMinutes = maxOf(currentMinutes, timeToMinutes(block.scheduledTime) + block.durationMinutes)
            } else {
                var proposedStart = maxOf(currentMinutes, timeToMinutes(block.scheduledTime))
                var proposedEnd = proposedStart + block.durationMinutes

                var conflict = true
                while (conflict) {
                    conflict = false
                    for (j in i + 1 until habits.size) {
                        val futureBlock = habits[j]
                        if (futureBlock.isImmutable) {
                            val futureStart = timeToMinutes(futureBlock.scheduledTime)
                            val futureEnd = futureStart + futureBlock.durationMinutes
                            if (proposedStart < futureEnd && proposedEnd > futureStart) {
                                proposedStart = futureEnd
                                proposedEnd = proposedStart + block.durationMinutes
                                conflict = true
                                break
                            }
                        }
                    }
                }

                block.scheduledTime = minutesToTime(proposedStart)
                currentMinutes = proposedEnd
            }
        }
        habits.sortBy { timeToMinutes(it.scheduledTime) }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun minutesToTime(minutes: Int): String {
        val normalized = minutes % (24 * 60)
        val h = normalized / 60
        val m = normalized % 60
        return "%02d:%02d".format(h, m)
    }

    /**
     * Thread-safe helper for mutating the habit list.
     * Always creates a new list reference to guarantee Compose recomposition.
     */
    private fun mutateHabitBlocks(transform: MutableList<HabitBlock>.() -> Unit) {
        val mutable = _habitBlocks.value.map { it.copy() }.toMutableList()
        mutable.transform()
        resolveCollisions(mutable)
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
        val timings = NotificationConfig.HAPTIC_PATTERN
        val amplitudes = NotificationConfig.HAPTIC_AMPLITUDES

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
                        "¿Prefieres mover la lectura 30 minutos más tarde?"
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Hola. Notamos que este horario te ha costado trabajo últimamente. " +
                            "No te preocupes, vamos a tu ritmo. ¿Prefieres mover la lectura " +
                            "30 minutos más tarde o prefieres que lo intentemos el sábado por la mañana?"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_today, "Posponer 30 min", actionPendingIntent)
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
