package com.uam.routin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.data.model.HabitBlock
import com.uam.routin.ui.theme.RoutInColors
import com.uam.routin.viewmodel.RoutInViewModel
import com.uam.routin.viewmodel.UiState
import kotlinx.coroutines.launch
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
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: RoutInViewModel) {

    val habitBlocks by viewModel.habitBlocks
    val uiState by viewModel.uiState

    // SPEC05 — Add Habit Dialog visibility state
    var showAddDialog by remember { mutableStateOf(false) }

    // SPEC07 — Edit/Delete Habit Dialog visibility state and target block
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedHabit by remember { mutableStateOf<HabitBlock?>(null) }

    // SPEC09 — Conversational AI Bottom Sheet state
    var showBottomSheet by remember { mutableStateOf(false) }
    var commandResponse by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isVoiceActive = uiState == UiState.Listening || uiState == UiState.Loading || uiState == UiState.Speaking

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
                    contentColor = RoutInColors.DeepPurpleNavy
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Agregar hábito",
                        modifier = Modifier.size(26.dp)
                    )
                }

                // ── Microphone FAB (SPEC09) — Opens Conversational Bottom Sheet ──
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
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
                        contentDescription = "Abrir asistente conversacional",
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

            // ── SPEC08: AI Copilot Progress Card ──────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AICopilotCard(viewModel = viewModel, uiState = uiState)
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
                        onClick = { viewModel.toggleHabitCompletion(block.id) },
                        onLongClick = {
                            selectedHabit = block
                            showEditDialog = true
                        }
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

    // ── SPEC07: Edit Habit Dialog ─────────────────────────────────────────────
    if (showEditDialog && selectedHabit != null) {
        EditHabitDialog(
            habit = selectedHabit!!,
            onDismiss = {
                showEditDialog = false
                selectedHabit = null
            },
            onConfirm = { name, time, duration, isImmutable ->
                viewModel.updateCustomHabit(
                    id = selectedHabit!!.id,
                    name = name,
                    time = time,
                    duration = duration,
                    isImmutable = isImmutable
                )
                showEditDialog = false
                selectedHabit = null
            },
            onDelete = {
                viewModel.deleteCustomHabit(selectedHabit!!.id)
                showEditDialog = false
                selectedHabit = null
            }
        )
    }

    // ── SPEC09: Conversational AI Bottom Sheet ────────────────────────────────
    if (showBottomSheet) {
        ConversationalBottomSheet(
            uiState = uiState,
            responseText = commandResponse,
            onDismiss = {
                showBottomSheet = false
                commandResponse = null
            },
            onSendCommand = { input ->
                commandResponse = null
                viewModel.speakAndProcess(input) { result ->
                    commandResponse = result.responseText
                }
            },
            onVoicePressStart = {
                commandResponse = null
                viewModel.onMicPressStart()
            },
            onVoicePressRelease = {
                viewModel.onMicRelease()
            }
        )
    }
}

// ─── SPEC08: AI Copilot Progress Card ─────────────────────────────────────────

/**
 * Premium AI Copilot & Progress Hub card that replaces the static header.
 * Displays dynamic coaching messages, a completion progress bar, and a streak counter.
 * All derived state comes from the ViewModel's reactive `derivedStateOf` properties.
 *
 * testId: card_ai_copilot
 */
@Composable
private fun AICopilotCard(viewModel: RoutInViewModel, uiState: UiState) {
    val coachingMessage by viewModel.coachingMessage
    val completionProgress by viewModel.completionProgress
    val completionLabel by viewModel.completionLabel

    val animatedProgress by animateFloatAsState(
        targetValue = completionProgress,
        animationSpec = tween(durationMillis = 600),
        label = "progress_anim"
    )

    // Dynamic subtitle based on voice state
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "card_ai_copilot" },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RoutInColors.DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ── Greeting Row ──────────────────────────────────────────────
            Text(
                text = "Buenos días, Gabriel 👋",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = RoutInColors.OffWhiteSerenity
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Progress Bar ──────────────────────────────────────────────
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = RoutInColors.VibrantGreenEmphasis,
                trackColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ── Stats Row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = completionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = RoutInColors.VibrantGreenEmphasis,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "🔥 Racha de ${viewModel.streakDays} días",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = RoutInColors.OptimismYellow,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── AI Coaching Message ───────────────────────────────────────
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "💡",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                )
                Text(
                    text = coachingMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoutInColors.OffWhiteSerenity.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitBlockCard(
    block: HabitBlock,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

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
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .semantics { contentDescription = semanticId }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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

// ─── Debug Dashboard (SPEC08: Collapsible) ───────────────────────────────────

/**
 * Collapsible debug panel for the SIE 2026 presentation.
 * Starts minimized to reduce visual clutter on the main feed.
 * Exposes explicit simulation triggers for SPEC03 and SPEC04.
 * All calls are forwarded directly to the ViewModel — zero inline logic.
 *
 * testId: section_debug
 */
@Composable
private fun DebugDashboard(viewModel: RoutInViewModel) {
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "section_debug" },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RoutInColors.DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Collapsible Header Row ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = RoutInColors.SoftMutedLavender,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(chevronRotation)
                )
            }

            // ── Expandable Content ────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200))
            ) {
                Column {
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

// ─── SPEC07: Edit Habit Dialog ───────────────────────────────────────────────

/**
 * Material Design 3 AlertDialog for in-memory habit editing and deletion.
 * Prepopulated with the target [habit]'s current properties.
 * Includes an "Immutable" toggle to anchor the block in the schedule and
 * a soft-red Delete action. All mutations are delegated to the ViewModel.
 *
 * testId: dialog_edit_habit
 */
@Composable
private fun EditHabitDialog(
    habit: HabitBlock,
    onDismiss: () -> Unit,
    onConfirm: (name: String, time: String, duration: Int, isImmutable: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var habitName by remember { mutableStateOf(habit.name) }
    var scheduledTime by remember { mutableStateOf(habit.scheduledTime) }
    var durationText by remember { mutableStateOf(habit.durationMinutes.toString()) }
    var isImmutable by remember { mutableStateOf(habit.isImmutable) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scheduledTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            },
            habit.scheduledTime.substringBefore(":").toIntOrNull() ?: 12,
            habit.scheduledTime.substringAfter(":").toIntOrNull() ?: 0,
            true
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
        modifier = Modifier.semantics { contentDescription = "dialog_edit_habit" },
        containerColor = RoutInColors.DarkSurface,
        titleContentColor = RoutInColors.OffWhiteSerenity,
        textContentColor = RoutInColors.SoftMutedLavender,
        title = {
            Text(
                text = "Editar Hábito",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Nombre del hábito") },
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )
                // ── Immutable Toggle ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Fijo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = RoutInColors.OffWhiteSerenity
                            )
                        )
                        Text(
                            text = "Ancla este bloque al horario",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoutInColors.SoftMutedLavender.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = isImmutable,
                        onCheckedChange = { isImmutable = it },
                        modifier = Modifier.semantics { contentDescription = "toggle_immutable" },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RoutInColors.DeepPurpleNavy,
                            checkedTrackColor = RoutInColors.VibrantGreenEmphasis,
                            uncheckedThumbColor = RoutInColors.SoftMutedLavender,
                            uncheckedTrackColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: habit.durationMinutes
                    val time = if (scheduledTime.matches(Regex("\\d{2}:\\d{2}"))) {
                        scheduledTime
                    } else {
                        habit.scheduledTime
                    }
                    if (habitName.isNotBlank()) {
                        onConfirm(habitName.trim(), time, duration, isImmutable)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutInColors.VibrantGreenEmphasis,
                    contentColor = RoutInColors.DeepPurpleNavy
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = habitName.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Delete Action ─────────────────────────────────────────────
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "btn_delete_habit" }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color(0xFFE57373),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancelar",
                        color = RoutInColors.SoftMutedLavender
                    )
                }
            }
        }
    )
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

// ─── SPEC09: Conversational AI Bottom Sheet ──────────────────────────────────

/**
 * Interactive conversational interface presented as a ModalBottomSheet.
 * Allows the jury to type natural-language schedule commands or tap
 * quick suggestion chips. Commands are processed by the ViewModel's
 * local regex-based NLP engine.
 *
 * testId: sheet_conversational
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationalBottomSheet(
    uiState: UiState,
    responseText: String?,
    onDismiss: () -> Unit,
    onSendCommand: (String) -> Unit,
    onVoicePressStart: () -> Unit,
    onVoicePressRelease: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val isProcessing = uiState == UiState.Loading || uiState == UiState.Speaking

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RoutInColors.DarkSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        RoutInColors.SoftMutedLavender.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .semantics { contentDescription = "sheet_conversational" }
        ) {
            // ── Title ────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = null,
                    tint = RoutInColors.VibrantGreenEmphasis,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hablemos con Rout-In",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = RoutInColors.OffWhiteSerenity
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Escribe o dicta un comando",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RoutInColors.SoftMutedLavender.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Input Field ──────────────────────────────────────────────────
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "input_command" },
                placeholder = {
                    Text(
                        "mueve mi gimnasio a las 8...",
                        color = RoutInColors.SoftMutedLavender.copy(alpha = 0.4f)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isProcessing) {
                                onSendCommand(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Enviar comando",
                            tint = if (inputText.isNotBlank() && !isProcessing)
                                RoutInColors.VibrantGreenEmphasis
                            else RoutInColors.SoftMutedLavender.copy(alpha = 0.3f)
                        )
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = RoutInColors.OffWhiteSerenity,
                    unfocusedTextColor = RoutInColors.SoftMutedLavender,
                    cursorColor = RoutInColors.VibrantGreenEmphasis,
                    focusedBorderColor = RoutInColors.VibrantGreenEmphasis,
                    unfocusedBorderColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Quick Suggestion Chips ───────────────────────────────────────
            Text(
                text = "Sugerencias rápidas:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = RoutInColors.SoftMutedLavender.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSuggestionChip(
                    label = "📅 Mover",
                    modifier = Modifier.weight(1f),
                    onClick = { inputText = "mueve gym a las 20:00" }
                )
                QuickSuggestionChip(
                    label = "✅ Completar",
                    modifier = Modifier.weight(1f),
                    onClick = { inputText = "ya terminé" }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSuggestionChip(
                    label = "🆕 Agregar",
                    modifier = Modifier.weight(1f),
                    onClick = { inputText = "agrega estudio de cálculo a las 21:00 por 45 min" }
                )
                QuickSuggestionChip(
                    label = "🗑 Cancelar",
                    modifier = Modifier.weight(1f),
                    onClick = { inputText = "cancela gym" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Press-and-Hold Voice Button ─────────────────────────────────
            var isVoiceButtonPressed by remember { mutableStateOf(false) }
            val voiceButtonScale by animateFloatAsState(
                targetValue = if (isVoiceButtonPressed) 0.95f else 1f,
                animationSpec = tween(100),
                label = "voice_btn_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(voiceButtonScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isProcessing -> RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.35f)
                            isVoiceButtonPressed -> RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.75f)
                            else -> RoutInColors.VibrantGreenEmphasis
                        }
                    )
                    .semantics { contentDescription = "btn_voice_simulate" }
                    .pointerInput(isProcessing) {
                        if (!isProcessing) {
                            detectTapGestures(
                                onPress = {
                                    isVoiceButtonPressed = true
                                    onVoicePressStart()
                                    tryAwaitRelease()
                                    isVoiceButtonPressed = false
                                    onVoicePressRelease()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isVoiceButtonPressed) Icons.Rounded.RecordVoiceOver else Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = RoutInColors.DeepPurpleNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isVoiceButtonPressed) "🔊 Escuchando..."
                               else "¿En qué te puedo ayudar?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = RoutInColors.DeepPurpleNavy,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Processing Indicator ─────────────────────────────────────────
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = RoutInColors.VibrantGreenEmphasis,
                        trackColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState == UiState.Speaking) "🔊 Respondiendo..." else "⏳ Procesando...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoutInColors.VibrantGreenEmphasis,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // ── AI Response ──────────────────────────────────────────────────
            val voiceSimText = "Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."
            val showResponse = responseText != null || uiState == UiState.Speaking || uiState is UiState.Success
            val finalResponseText = responseText ?: voiceSimText

            AnimatedVisibility(
                visible = showResponse,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                if (showResponse) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RoutInColors.DeepPurpleNavy
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "💬",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 10.dp, top = 1.dp)
                            )
                            Text(
                                text = finalResponseText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = RoutInColors.OffWhiteSerenity,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tappable quick-suggestion chip for the conversational bottom sheet.
 * Pre-fills the command input field with a template command.
 */
@Composable
private fun QuickSuggestionChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = RoutInColors.SoftMutedLavender.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = RoutInColors.OffWhiteSerenity,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
