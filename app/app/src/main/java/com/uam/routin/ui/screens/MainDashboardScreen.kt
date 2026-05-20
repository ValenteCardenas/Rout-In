package com.uam.routin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.data.model.HabitBlock
import com.uam.routin.ui.theme.RoutInColors
import com.uam.routin.viewmodel.RoutInViewModel
import com.uam.routin.viewmodel.UiState
import kotlin.math.PI
import kotlin.math.sin

/**
 * SPEC01–SPEC04 — MainDashboardScreen
 *
 * The primary screen of the Rout-In MVP. Renders the chronological habit block
 * feed and exposes the Debug Dashboard for presentation simulation triggers.
 *
 * The screen is fully declarative — all mutations are dispatched to [viewModel].
 * No business logic, coroutine calls, or hardware triggers live here.
 *
 * testId: screen_main_dashboard
 */
@Composable
fun MainDashboardScreen(viewModel: RoutInViewModel) {

    val habitBlocks by viewModel.habitBlocks
    val uiState by viewModel.uiState

    // SPEC05 — Add Habit Dialog visibility state
    var showAddDialog by remember { mutableStateOf(false) }

    val isVoiceActive = uiState == UiState.Listening || uiState == UiState.Loading || uiState == UiState.Speaking

    // Track precise touch events natively without pointerInput swallow issues
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            viewModel.onMicPressStart()
        } else {
            viewModel.onMicRelease()
        }
    }

    // FAB pulse scale — active only when voice is processing
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isVoiceActive) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Scaffold(
        modifier = Modifier.semantics { contentDescription = "screen_main_dashboard" },
        containerColor = RoutInColors.DeepPurpleNavy,
        floatingActionButton = {
            // ── FAB Slot: Column wrapper (SPEC05 §7.4) ─────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // ── Add Habit FAB (SPEC05) ──────────────────────────────────────
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(56.dp)
                        .semantics { contentDescription = "fab_add_habit" },
                    shape = CircleShape,
                    containerColor = RoutInColors.ClarityBlue,
                    contentColor = RoutInColors.OffWhiteSerenity
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Agregar hábito",
                        modifier = Modifier.size(26.dp)
                    )
                }

                // ── Microphone FAB (SPEC02) ──────────────────────────────────────
                FloatingActionButton(
                    onClick = { /* empty, gesture handled natively by interactionSource */ },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(fabScale)
                        .semantics { contentDescription = "fab_microphone" },
                    shape = CircleShape,
                    containerColor = RoutInColors.VibrantGreenEmphasis,
                    contentColor = RoutInColors.DeepPurpleNavy
                ) {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Rounded.RecordVoiceOver else Icons.Rounded.Mic,
                        contentDescription = "Simular comando de voz",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                DashboardHeader(uiState = uiState)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Wave Animation (SPEC02) — visible during all voice processing states ──
            item {
                AnimatedVisibility(
                    visible = isVoiceActive,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    WaveAnimationPanel(uiState = uiState)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Habit Block Cards ──────────────────────────────────────────────
            items(items = habitBlocks, key = { it.id }) { block ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    HabitBlockCard(
                        block = block,
                        onClick = { viewModel.toggleHabitCompletion(block.id) }
                    )
                }
            }

            // ── Debug Dashboard Section ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DebugDashboard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(80.dp)) // FAB clearance
            }
        }
    }

    // ── SPEC05: Add Habit Dialog ──────────────────────────────────────────────
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, time, duration ->
                viewModel.addCustomHabit(name, time, duration)
                showAddDialog = false
            }
        )
    }
}

// ─── Dashboard Header ─────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(uiState: UiState) {
    Column {
        Text(
            text = "Buenos días, Gabriel 👋",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = RoutInColors.OffWhiteSerenity
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        val subtitle = when (uiState) {
            is UiState.Listening -> "🎙 Escuchando tu comando…"
            is UiState.Loading -> "⏳ Procesando con IA…"
            is UiState.Speaking -> "🔊 Respondiendo…"
            is UiState.Success -> "✅ Agenda actualizada"
            else -> "Tu agenda de hoy · ${java.time.LocalDate.now().let {
                "${it.dayOfMonth}/${it.monthValue}/${it.year}"
            }}"
        }
        val subtitleColor = when (uiState) {
            is UiState.Listening, is UiState.Loading, is UiState.Speaking ->
                RoutInColors.VibrantGreenEmphasis
            is UiState.Success -> RoutInColors.VibrantGreenEmphasis
            else -> RoutInColors.SoftMutedLavender
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = subtitleColor,
                fontSize = 14.sp
            )
        )
    }
}

// ─── Wave Animation Panel (SPEC02) ───────────────────────────────────────────

/**
 * Premium Compose Canvas wave animation component.
 * Renders three overlapping sinusoidal wave layers with distinct frequencies,
 * amplitudes, and color opacities to simulate an active audio capture stream.
 * Active during [UiState.Listening] and [UiState.Loading] states.
 *
 * testId: anim_wave
 */
@Composable
private fun WaveAnimationPanel(uiState: UiState) {
    // Phase offset animated at different speeds for each wave layer
    val phase = remember { Animatable(0f) }
    LaunchedEffect(uiState) {
        phase.animateTo(
            targetValue = (2 * PI).toFloat() * 100,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    val waveColor1 = RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.85f)
    val waveColor2 = RoutInColors.SoftMutedLavender.copy(alpha = 0.45f)
    val waveColor3 = RoutInColors.ClarityBlue.copy(alpha = 0.30f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { contentDescription = "anim_wave" }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawWaveLayer(
                phaseOffset = phase.value,
                amplitude = size.height * 0.28f,
                frequency = 1.4f,
                color = waveColor1,
                strokeWidth = 3.5f
            )
            drawWaveLayer(
                phaseOffset = phase.value * 0.75f,
                amplitude = size.height * 0.20f,
                frequency = 2.1f,
                color = waveColor2,
                strokeWidth = 2.5f
            )
            drawWaveLayer(
                phaseOffset = phase.value * 1.3f,
                amplitude = size.height * 0.14f,
                frequency = 3.0f,
                color = waveColor3,
                strokeWidth = 2.0f
            )
        }
        // Glowing label overlay
        Text(
            text = when (uiState) {
                is UiState.Speaking -> "\"Tengo una junta con mi asesor de Proyecto de Investigación a las 5…\""
                else -> "\"Tengo una junta con mi asesor de Proyecto de Investigación a las 5…\""
            },
            style = MaterialTheme.typography.labelSmall.copy(
                color = RoutInColors.SoftMutedLavender.copy(alpha = 0.7f),
                fontSize = 10.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp, start = 8.dp, end = 8.dp),
            maxLines = 1
        )
    }
}

/**
 * Draws a single sinusoidal wave path across the full canvas width.
 * Used by [WaveAnimationPanel] to create layered depth.
 */
private fun DrawScope.drawWaveLayer(
    phaseOffset: Float,
    amplitude: Float,
    frequency: Float,
    color: Color,
    strokeWidth: Float
) {
    val width = size.width
    val centerY = size.height / 2f
    val path = Path()
    val step = 4f
    var x = 0f
    path.moveTo(x, centerY + amplitude * sin(frequency * x / width * 2 * PI + phaseOffset).toFloat())
    while (x <= width) {
        val y = centerY + amplitude * sin(frequency * x / width * 2 * PI.toFloat() + phaseOffset).toFloat()
        path.lineTo(x, y)
        x += step
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}

// ─── Habit Block Card ─────────────────────────────────────────────────────────

/**
 * Dynamic card that selects its visual variant based on [HabitBlock.status] and [HabitBlock.source].
 * Variants: Standard · External-Immutable · Friction · Pending-Reallocation · Completed · Reallocated
 *
 * testId: card_habit_{block.id} (standard)
 *         card_habit_friction_{block.id}
 *         card_habit_external_{block.id}
 *         card_habit_pending_{block.id}
 */
@Composable
private fun HabitBlockCard(block: HabitBlock, onClick: () -> Unit = {}) {

    val (cardBackground, textOnCard) = resolveCardColors(block)
    val cardAlpha = if (block.status == HabitBlock.StatusConstants.PENDING_REALLOCATION) 0.5f else 1f

    val animatedBackground by animateColorAsState(
        targetValue = cardBackground,
        animationSpec = tween(durationMillis = 500),
        label = "card_bg_anim_${block.id}"
    )

    // Dynamic semantic testId based on card variant (SPEC02–SPEC04 testing contract)
    val semanticId = when {
        block.status == HabitBlock.StatusConstants.FRICTION ->
            "card_habit_friction_${block.id}"
        block.source == HabitBlock.Source.EXTERNAL && block.isImmutable ->
            "card_habit_external_${block.id}"
        block.status == HabitBlock.StatusConstants.PENDING_REALLOCATION ->
            "card_habit_pending_${block.id}"
        else -> "card_habit_${block.id}"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .semantics { contentDescription = semanticId },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Status icon indicator circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(resolveStatusAccent(block).copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveStatusIcon(block),
                    contentDescription = block.status,
                    tint = resolveStatusAccent(block),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = textOnCard,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = block.scheduledTime,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = textOnCard.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    )
                    if (block.durationMinutes > 0) {
                        Text(
                            text = " · ${block.durationMinutes} min",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = textOnCard.copy(alpha = 0.55f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Trailing status badge
            StatusBadge(block = block, textColor = textOnCard)
        }
    }
}

@Composable
private fun StatusBadge(block: HabitBlock, textColor: Color) {
    val badgeLabel = when (block.status) {
        HabitBlock.StatusConstants.COMPLETED -> "✓ Listo"
        HabitBlock.StatusConstants.FRICTION -> "⚠ Fricción"
        HabitBlock.StatusConstants.REALLOCATED -> "↗ Reubicado"
        HabitBlock.StatusConstants.PENDING_REALLOCATION -> "🕐 Pendiente"
        else -> when {
            block.isImmutable && block.source == HabitBlock.Source.EXTERNAL -> "🔒 Externo"
            block.isImmutable -> "📌 Fijo"
            else -> ""
        }
    }
    if (badgeLabel.isNotEmpty()) {
        Text(
            text = badgeLabel,
            style = MaterialTheme.typography.labelLarge.copy(
                color = textColor.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Returns (cardBackground, onCardTextColor) based on status and source.
 * When a pastel color is used as background, text automatically swaps to
 * [RoutInColors.DeepPurpleNavy] per the design system contrast rule.
 */
private fun resolveCardColors(block: HabitBlock): Pair<Color, Color> {
    return when {
        block.source == HabitBlock.Source.EXTERNAL && block.isImmutable ->
            RoutInColors.ClarityBlue to RoutInColors.DeepPurpleNavy
        block.status == HabitBlock.StatusConstants.COMPLETED ->
            RoutInColors.WellbeingMint to RoutInColors.DeepPurpleNavy
        block.status == HabitBlock.StatusConstants.FRICTION ->
            RoutInColors.OptimismYellow to RoutInColors.DeepPurpleNavy
        block.status == HabitBlock.StatusConstants.REALLOCATED ->
            RoutInColors.WellbeingMint to RoutInColors.DeepPurpleNavy
        block.status == HabitBlock.StatusConstants.PENDING_REALLOCATION ->
            RoutInColors.DarkSurface to RoutInColors.SoftMutedLavender
        else -> RoutInColors.DarkSurface to RoutInColors.OffWhiteSerenity
    }
}

private fun resolveStatusAccent(block: HabitBlock): Color = when (block.status) {
    HabitBlock.StatusConstants.COMPLETED -> RoutInColors.VibrantGreenEmphasis
    HabitBlock.StatusConstants.FRICTION -> Color(0xFFFFB347)
    HabitBlock.StatusConstants.REALLOCATED -> RoutInColors.VibrantGreenEmphasis
    HabitBlock.StatusConstants.PENDING_REALLOCATION -> Color(0xFFB0AACC)
    else -> if (block.source == HabitBlock.Source.EXTERNAL)
        RoutInColors.ClarityBlue else RoutInColors.SoftMutedLavender
}

@Composable
private fun resolveStatusIcon(block: HabitBlock) = when {
    block.status == HabitBlock.StatusConstants.COMPLETED -> Icons.Rounded.Check
    block.status == HabitBlock.StatusConstants.FRICTION -> Icons.Rounded.Warning
    block.status == HabitBlock.StatusConstants.PENDING_REALLOCATION -> Icons.Rounded.Schedule
    block.source == HabitBlock.Source.EXTERNAL && block.isImmutable -> Icons.Rounded.Lock
    block.isImmutable -> Icons.Rounded.CalendarToday
    else -> Icons.Rounded.Schedule
}

// ─── Debug Dashboard ──────────────────────────────────────────────────────────

/**
 * Accessible debug panel for the SIE 2026 presentation.
 * Exposes explicit simulation triggers for SPEC03 and SPEC04.
 * All calls are forwarded directly to the ViewModel — zero inline logic.
 *
 * testId: section_debug
 */
@Composable
private fun DebugDashboard(viewModel: RoutInViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "section_debug" },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RoutInColors.DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = null,
                    tint = RoutInColors.SoftMutedLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Debug Dashboard · Simulaciones",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = RoutInColors.SoftMutedLavender,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SPEC03 trigger — btn_simulate_friction
            DebugButton(
                label = "Simulate Critical Friction",
                description = "Marca Reading Block (id=108) como FRICTION y dispara notificación",
                containerColor = RoutInColors.OptimismYellow,
                testId = "btn_simulate_friction",
                onClick = { viewModel.simulateCriticalFriction() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SPEC04 trigger — btn_simulate_mcp
            DebugButton(
                label = "Simulate MCP Collision",
                description = "Inyecta 'Sistemas Operativos Exam' y desplaza Gym (id=106)",
                containerColor = RoutInColors.ClarityBlue,
                testId = "btn_simulate_mcp",
                onClick = { viewModel.simulateMcpCollision() }
            )
        }
    }
}

@Composable
private fun DebugButton(
    label: String,
    description: String,
    containerColor: Color,
    testId: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = testId },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = RoutInColors.DeepPurpleNavy
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
        )
    }
}

// ─── SPEC05: Add Habit Dialog ─────────────────────────────────────────────────

/**
 * Material Design 3 AlertDialog for manual in-memory habit creation.
 * Gathers Habit Name, Scheduled Time (HH:mm), and Duration (minutes)
 * from the user. All state mutation is delegated to the ViewModel via [onConfirm].
 *
 * testId: dialog_add_habit
 */
@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, time: String, duration: Int) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("12:00") }
    var durationText by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scheduledTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            },
            12, 0, true
        )
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = RoutInColors.OffWhiteSerenity,
        unfocusedTextColor = RoutInColors.SoftMutedLavender,
        cursorColor = RoutInColors.VibrantGreenEmphasis,
        focusedBorderColor = RoutInColors.VibrantGreenEmphasis,
        unfocusedBorderColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.4f),
        focusedLabelColor = RoutInColors.VibrantGreenEmphasis,
        unfocusedLabelColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.7f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics { contentDescription = "dialog_add_habit" },
        containerColor = RoutInColors.DarkSurface,
        titleContentColor = RoutInColors.OffWhiteSerenity,
        textContentColor = RoutInColors.SoftMutedLavender,
        title = {
            Text(
                text = "Nuevo Hábito",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Agrega una nueva rutina a tu agenda",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoutInColors.SoftMutedLavender.copy(alpha = 0.7f)
                    )
                )
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Nombre del hábito") },
                    placeholder = { Text("Ej: Repasar Sistemas Operativos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = scheduledTime,
                    onValueChange = { },
                    label = { Text("Hora (HH:mm)") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = { timePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Seleccionar Hora",
                                tint = RoutInColors.SoftMutedLavender
                            )
                        }
                    },
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                        timePickerDialog.show()
                                    }
                                }
                            }
                        }
                )
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 3) durationText = filtered
                    },
                    label = { Text("Duración (minutos)") },
                    placeholder = { Text("Ej: 60") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 30
                    val time = if (scheduledTime.matches(Regex("\\d{2}:\\d{2}"))) {
                        scheduledTime
                    } else {
                        "12:00" // Fallback default
                    }
                    if (habitName.isNotBlank()) {
                        onConfirm(habitName.trim(), time, duration)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutInColors.VibrantGreenEmphasis,
                    contentColor = RoutInColors.DeepPurpleNavy
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = habitName.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = RoutInColors.SoftMutedLavender
                )
            }
        }
    )
}
