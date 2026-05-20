package com.uam.routin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.data.model.HabitBlock
import com.uam.routin.ui.theme.RoutInColors
import com.uam.routin.viewmodel.RoutInViewModel
import com.uam.routin.viewmodel.VoiceUiState

/**
 * SPEC01–SPEC04 — MainDashboardScreen
 *
 * The primary screen of the Rout-In MVP. Renders the chronological habit block
 * feed and exposes the Debug Dashboard for presentation simulation triggers.
 *
 * The screen is fully declarative — all mutations are dispatched to [viewModel].
 * No business logic, coroutine calls, or hardware triggers live here.
 */
@Composable
fun MainDashboardScreen(viewModel: RoutInViewModel) {

    val habitBlocks by viewModel.habitBlocks
    val voiceUiState by viewModel.voiceUiState

    // FAB pulse animation active only during voice processing
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceUiState == VoiceUiState.PROCESSING) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Scaffold(
        containerColor = RoutInColors.DeepPurpleNavy,
        floatingActionButton = {
            // ── Voice Simulation FAB (SPEC02) ─────────────────────────────────
            FloatingActionButton(
                onClick = { viewModel.simulateVoiceCommand() },
                modifier = Modifier
                    .size(64.dp)
                    .scale(fabScale)
                    .semantics { contentDescription = "fab_voice" },
                shape = CircleShape,
                containerColor = RoutInColors.VibrantGreenEmphasis,
                contentColor = RoutInColors.DeepPurpleNavy
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Simular comando de voz",
                    modifier = Modifier.size(28.dp)
                )
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
                DashboardHeader(voiceUiState = voiceUiState)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Habit Block Cards ──────────────────────────────────────────────
            items(items = habitBlocks, key = { it.id }) { block ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    HabitBlockCard(block = block)
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
}

// ─── Dashboard Header ─────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(voiceUiState: VoiceUiState) {
    Column {
        Text(
            text = "Buenos días, Gabriel 👋",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = RoutInColors.OffWhiteSerenity
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (voiceUiState == VoiceUiState.PROCESSING)
                "🎙 Procesando tu comando…"
            else
                "Tu agenda de hoy · ${java.time.LocalDate.now().let {
                    "${it.dayOfMonth}/${it.monthValue}/${it.year}"
                }}",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (voiceUiState == VoiceUiState.PROCESSING)
                    RoutInColors.VibrantGreenEmphasis
                else
                    RoutInColors.SoftMutedLavender,
                fontSize = 14.sp
            )
        )
    }
}

// ─── Habit Block Card ─────────────────────────────────────────────────────────

@Composable
private fun HabitBlockCard(block: HabitBlock) {

    // Determine background and text colors based on status and source
    val (cardBackground, textOnCard) = resolveCardColors(block)
    val animatedBackground by animateColorAsState(
        targetValue = cardBackground,
        animationSpec = tween(durationMillis = 500),
        label = "card_bg_anim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "habit_card_${block.id}" },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Status icon indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(resolveStatusAccent(block).copy(alpha = 0.2f), CircleShape),
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

            // Status badge
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
        HabitBlock.StatusConstants.PENDING_REALLOCATION -> "⏳ Pendiente"
        else -> if (block.isImmutable && block.source == HabitBlock.Source.EXTERNAL) "📅 Externo" else ""
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

/** Returns (cardBackground, onCardTextColor) based on status and source. */
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
            RoutInColors.OptimismYellow to RoutInColors.DeepPurpleNavy
        else -> RoutInColors.DarkSurface to RoutInColors.OffWhiteSerenity
    }
}

private fun resolveStatusAccent(block: HabitBlock): Color = when (block.status) {
    HabitBlock.StatusConstants.COMPLETED -> RoutInColors.VibrantGreenEmphasis
    HabitBlock.StatusConstants.FRICTION -> Color(0xFFFFB347)
    HabitBlock.StatusConstants.REALLOCATED -> RoutInColors.VibrantGreenEmphasis
    HabitBlock.StatusConstants.PENDING_REALLOCATION -> Color(0xFFFFB347)
    else -> if (block.source == HabitBlock.Source.EXTERNAL)
        RoutInColors.ClarityBlue else RoutInColors.SoftMutedLavender
}

@Composable
private fun resolveStatusIcon(block: HabitBlock) = when (block.status) {
    HabitBlock.StatusConstants.COMPLETED -> Icons.Rounded.Check
    HabitBlock.StatusConstants.FRICTION,
    HabitBlock.StatusConstants.PENDING_REALLOCATION -> Icons.Rounded.Warning
    else -> Icons.Rounded.Schedule
}

// ─── Debug Dashboard ──────────────────────────────────────────────────────────

/**
 * Accessible debug panel for the SIE 2026 presentation.
 * Exposes explicit simulation triggers for SPEC03 and SPEC04.
 * All calls are forwarded directly to the ViewModel — zero inline logic.
 */
@Composable
private fun DebugDashboard(viewModel: RoutInViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "debug_dashboard" },
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

            // SPEC03 trigger
            DebugButton(
                label = "Simulate Critical Friction",
                description = "Marca Reading Block como FRICTION y dispara notificación",
                containerColor = RoutInColors.OptimismYellow,
                testId = "btn_simulate_friction",
                onClick = { viewModel.simulateCriticalFriction() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SPEC04 trigger
            DebugButton(
                label = "Simulate MCP Collision",
                description = "Inyecta 'Sistemas Operativos Exam' y desplaza Gym",
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
            )
        }
    }
}
