package com.uam.routin.ui.screens.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.ui.theme.RoutInColors

/**
 * SPEC01 — WelcomeScreen
 *
 * First screen the user sees on a fresh install. Displays Rout-In branding and
 * a simulated "Authenticate" button. No network call is made — tapping immediately
 * transitions to [DeploymentSelectionScreen].
 *
 * Test ID: screen_welcome
 */
@Composable
fun WelcomeScreen(onAuthenticate: () -> Unit) {

    // Pulsing glow animation for the logo accent orb
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutInColors.DeepPurpleNavy)
            .semantics { contentDescription = "screen_welcome" }
    ) {
        // Subtle radial gradient backdrop for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.07f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Pulsing logo orb ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(orbScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.4f),
                                RoutInColors.VibrantGreenEmphasis.copy(alpha = 0.0f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(RoutInColors.VibrantGreenEmphasis, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Brand name ────────────────────────────────────────────────────
            Text(
                text = "Rout-In",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = RoutInColors.OffWhiteSerenity,
                    fontSize = 42.sp,
                    letterSpacing = (-1).sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tu copiloto de hábitos inteligente",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = RoutInColors.SoftMutedLavender
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Organiza tu día con voz. Sin formularios.\nSin fricción.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = RoutInColors.SoftMutedLavender.copy(alpha = 0.7f),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // ── Authenticate CTA ─────────────────────────────────────────────
            Button(
                onClick = onAuthenticate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "btn_authenticate" },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutInColors.VibrantGreenEmphasis,
                    contentColor = RoutInColors.DeepPurpleNavy
                )
            ) {
                Text(
                    text = "Autenticar",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MVP · SIE 2026 · UAM Iztapalapa",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = RoutInColors.SoftMutedLavender.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            )
        }
    }
}
