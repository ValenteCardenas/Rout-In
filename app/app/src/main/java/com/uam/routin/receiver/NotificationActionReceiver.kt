package com.uam.routin.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uam.routin.util.NotificationHelper
import com.uam.routin.data.model.NotificationConfig

/**
 * BroadcastReceiver that intercepts one-tap interactive notification action buttons.
 *
 * Supported actions:
 * - [ACTION_MOVE_HABIT_630]    → Triggered by "Move to 6:30 PM" (SPEC03 Friction flow)
 * - [ACTION_RELOCATE_HABIT_730] → Triggered by "Re-locate to 7:30 PM" (SPEC04 MCP flow)
 *
 * Communication with the ViewModel is achieved via the companion object callback lambdas,
 * which the ViewModel registers during initialization and clears in onCleared().
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MOVE_HABIT_630 = NotificationConfig.ACTION_MOVE_HABIT_630
        const val ACTION_RELOCATE_HABIT_730 = NotificationConfig.ACTION_RELOCATE_730
        const val EXTRA_NOTIFICATION_ID = NotificationConfig.EXTRA_NOTIFICATION_ID

        /**
         * Callback hooks registered by the ViewModel.
         * Using lambdas avoids a direct ViewModel reference in the receiver,
         * keeping this component lifecycle-safe for the MVP scope.
         */
        var onMoveHabit630: (() -> Unit)? = null
        var onRelocateHabit730: (() -> Unit)? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        val notifId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, NotificationHelper.NOTIF_ID_FRICTION)
            ?: NotificationHelper.NOTIF_ID_FRICTION

        // Dismiss the originating notification tray entry
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notifId)

        // Dispatch the appropriate state mutation callback to the ViewModel
        when (intent?.action) {
            ACTION_MOVE_HABIT_630 -> onMoveHabit630?.invoke()
            ACTION_RELOCATE_HABIT_730 -> onRelocateHabit730?.invoke()
        }
    }
}
