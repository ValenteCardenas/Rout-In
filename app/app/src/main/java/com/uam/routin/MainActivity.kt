package com.uam.routin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.uam.routin.ui.navigation.AppNavigation
import com.uam.routin.ui.theme.RoutInTheme
import com.uam.routin.util.NotificationHelper

/**
 * Single-activity entry point for the Rout-In MVP.
 *
 * Responsibilities on startup (in order):
 * 1. Create the "rout_in_behavioral_alerts" NotificationChannel (required by SPEC03, SPEC04).
 * 2. Request POST_NOTIFICATIONS runtime permission on Android 13+.
 * 3. Enable edge-to-edge display.
 * 4. Mount the AppNavigation Compose root, which owns the shared RoutInViewModel.
 */
class MainActivity : ComponentActivity() {

    // Runtime permission launcher for POST_NOTIFICATIONS (Android 13+ / API 33+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Permission result is informational only — MVP proceeds regardless
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1 — Bootstrap notification channel before any Compose content mounts
        NotificationHelper.createNotificationChannel(this)

        // Step 2 — Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()

        // Step 3 — Mount the full Compose navigation graph
        setContent {
            RoutInTheme {
                AppNavigation()
            }
        }
    }
}