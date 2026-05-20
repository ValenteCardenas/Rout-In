package com.uam.routin.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.ui.theme.RoutInColors

/**
 * SPEC01 — DeploymentSelectionScreen
 *
 * Presents two Material 3 selection cards for choosing the deployment mode:
 * - Open-Source Community (Self-Hosted) → navigates to LocalNetworkConfigScreen
 * - Premium Cloud Subscription → navigates to CloudOAuthMockScreen
 *
 * Test ID: screen_deployment_selection
 */
@Composable
fun DeploymentSelectionScreen(
    onSelectSelfHosted: () -> Unit,
    onSelectCloud: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "screen_deployment_selection" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Elige tu entorno",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¿Cómo conectará Rout-In con tu ecosistema?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Self-Hosted Card ──────────────────────────────────────────────
            DeploymentSelectionCard(
                testId = "card_self_hosted",
                icon = Icons.Rounded.Dns,
                title = "Open-Source Community",
                subtitle = "Self-Hosted",
                description = "Conecta tu propio servidor MCP en tu red local. Control total de tus datos.",
                accentColor = RoutInColors.ClarityBlue,
                onClick = onSelectSelfHosted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Cloud Card ────────────────────────────────────────────────────
            DeploymentSelectionCard(
                testId = "card_cloud",
                icon = Icons.Rounded.Cloud,
                title = "Premium Cloud",
                subtitle = "Subscription — Out-of-the-Box",
                description = "Conecta con Google Calendar y Outlook sin configuración. Listo en segundos.",
                accentColor = RoutInColors.WellbeingMint,
                onClick = onSelectCloud
            )
        }
    }
}

/**
 * Reusable Material 3 deployment mode card.
 * When a pastel [accentColor] is used as the icon background, typography
 * automatically swaps to [MaterialTheme.colorScheme.background] for accessibility contrast.
 */
@Composable
private fun DeploymentSelectionCard(
    testId: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = testId },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with pastel accent background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background, // High-contrast on pastel bg
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    ),
                    lineHeight = 19.sp
                )
            }
        }
    }
}
