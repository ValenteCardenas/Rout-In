package com.uam.routin.ui.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.ui.theme.RoutInColors

/**
 * SPEC01 — CloudOAuthMockScreen
 *
 * Displays a simulated OAuth provider list (Google, Outlook) with checkboxes.
 * No real OAuth flow is initiated — tapping "Continue" calls [onContinue] immediately.
 *
 * Test ID: screen_cloud_oauth
 */
@Composable
fun CloudOAuthMockScreen(onContinue: () -> Unit) {

    var googleChecked by remember { mutableStateOf(false) }
    var outlookChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutInColors.DeepPurpleNavy)
            .semantics { contentDescription = "screen_cloud_oauth" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Conecta tus cuentas",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = RoutInColors.OffWhiteSerenity
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Selecciona los calendarios que Rout-In puede leer para proteger tus compromisos automáticamente.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = RoutInColors.SoftMutedLavender,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Provider list ─────────────────────────────────────────────────
            OAuthProviderRow(
                icon = Icons.Rounded.AccountCircle,
                providerName = "Google Calendar",
                providerDetail = "google@uam.mx",
                accentColor = RoutInColors.ClarityBlue,
                checked = googleChecked,
                onCheckedChange = { googleChecked = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OAuthProviderRow(
                icon = Icons.Rounded.Mail,
                providerName = "Outlook / Microsoft 365",
                providerDetail = "outlook@uam.mx",
                accentColor = RoutInColors.WellbeingMint,
                checked = outlookChecked,
                onCheckedChange = { outlookChecked = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Simulación MVP — No se realizan conexiones reales.",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = RoutInColors.SoftMutedLavender.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutInColors.VibrantGreenEmphasis,
                    contentColor = RoutInColors.DeepPurpleNavy
                )
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
private fun OAuthProviderRow(
    icon: ImageVector,
    providerName: String,
    providerDetail: String,
    accentColor: androidx.compose.ui.graphics.Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RoutInColors.DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoutInColors.DeepPurpleNavy,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = providerName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = RoutInColors.OffWhiteSerenity
                    )
                )
                Text(
                    text = providerDetail,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = RoutInColors.SoftMutedLavender,
                        fontSize = 12.sp
                    )
                )
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = RoutInColors.VibrantGreenEmphasis,
                    uncheckedColor = RoutInColors.SoftMutedLavender,
                    checkmarkColor = RoutInColors.DeepPurpleNavy
                )
            )
        }
    }
}
