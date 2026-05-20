package com.uam.routin.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uam.routin.ui.theme.RoutInColors

/**
 * SPEC01 — LocalNetworkConfigScreen
 *
 * Allows the user to input their self-hosted MCP server URI.
 * The URI is passed back via [onSaveUri]; no real network connection is made.
 *
 * Test ID: screen_local_net_config
 */
@Composable
fun LocalNetworkConfigScreen(onSaveUri: (String) -> Unit) {

    // Local UI state for the text field — ephemeral, does not belong in ViewModel
    var uriInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "screen_local_net_config" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Servidor MCP Local",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingresa la URI de tu servidor MCP en la red Wi-Fi local.\nEsto conecta Rout-In con tu ecosistema personal.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = uriInput,
                onValueChange = { uriInput = it },
                label = {
                    Text(
                        "URI del Servidor MCP",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = {
                    Text(
                        "ws://192.168.x.x:8080/mcp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ejemplo: ws://192.168.1.100:8080/mcp",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    // Pass uri to ViewModel (even if empty for demo purposes)
                    onSaveUri(uriInput.ifBlank { "ws://localhost:8080/mcp" })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = "Guardar y Continuar",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                )
            }
        }
    }
}
