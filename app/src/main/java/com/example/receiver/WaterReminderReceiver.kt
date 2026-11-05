package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterReminderReceiver", "Alarm fired! Sending notification.")
        val pendingResult = goAsync()
        val db = com.example.data.HydrationDatabase.getDatabase(context.applicationContext)
        val repo = com.example.data.HydrationRepository(db.dao)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repo.getDirectUserSettings()
                showReminderNotification(context.applicationContext, settings)
            } catch (e: Exception) {
                Log.e("WaterReminderReceiver", "Error loading settings in alarm: ${e.message}", e)
                showReminderNotification(context.applicationContext, null)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showReminderNotification(context: Context, settings: com.example.data.UserSettings?) {
        val startAlertIntent = Intent(context, com.example.ReminderAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or 
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        var startedDirectly = false
        try {
            context.startActivity(startAlertIntent)
            Log.d("WaterReminderReceiver", "Alert screen started directly. Bypassing notification tray.")
            startedDirectly = true
        } catch (e: Exception) {
            Log.e("WaterReminderReceiver", "Failed to start activity directly: ${e.message}. Using high-priority notification as fallback.")
        }

        // If the screen alert started successfully, we satisfy the user request: No notification shown!
        if (startedDirectly) {
            return
        }

        // Fallback: Notify via status bar notification only if starting directly is restricted by OS
        if (settings?.isNotificationEnabled == false) {
            Log.d("WaterReminderReceiver", "Notifications are disabled in settings. Skipping fallback notification.")
            return
        }

        val soundUriString = settings?.reminderSoundUri
        val soundUri = if (!soundUriString.isNullOrEmpty()) {
            android.net.Uri.parse(soundUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        // Use dynamic channelId based on selected sound to avoid systems caching sound setting
        val channelId = "water_ch_v3_" + (soundUriString?.hashCode() ?: "default")
        val notificationId = 2002

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel with high importance for a loud alarm-style sound and popup banner if SDK 26+ (Android Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you dynamically when it's time to drink water based on your activity"
                enableLights(true)
                if (settings?.isVibrationEnabled != false) {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400) // alarming alarm pulses
                } else {
                    enableVibration(false)
                    vibrationPattern = null
                }
                setSound(soundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when user taps on invitation: Open MainActivity
        val openAppIntent = Intent(context, com.example.ReminderAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full-screen intent to automatically wake screen and overlay above keyguard
        val fullScreenIntent = Intent(context, com.example.ReminderAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            5001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build elegant Material-compatible high-importance alarm notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // stylish launch droplet
            .setContentTitle("Water Alert! 💧")
            .setContentText("It's time to take a sip of water! Stay energized and healthy.")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Max priority for full-screen interruption
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        if (settings?.isVibrationEnabled != false) {
            notificationBuilder.setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
        } else {
            notificationBuilder.setVibrate(null)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
