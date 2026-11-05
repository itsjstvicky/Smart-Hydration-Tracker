package com.example

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.*
import com.example.receiver.AlarmScheduler
import com.example.ui.components.WaterBottleCanvas
import com.example.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReminderAlertActivity : ComponentActivity() {
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: HydrationDatabase
    private lateinit var repository: HydrationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for premium immersive full screen layout
        enableEdgeToEdge()

        // Configure activity to wake up screen, show above lock screen, and stay on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        // Initialize Database and Repository directly to guarantee reliable standalone execution
        database = HydrationDatabase.getDatabase(applicationContext)
        repository = HydrationRepository(database.dao)

        // Load settings and start alert audio and vibration dynamically
        lifecycleScope.launch {
            var isVibrationOn = true
            try {
                val settings = repository.getDirectUserSettings()
                startAlertAudio(settings.reminderSoundUri)
                isVibrationOn = settings.isVibrationEnabled
            } catch (e: Exception) {
                Log.e("ReminderAlertActivity", "Fails to play alarm audio: ${e.message}")
                startAlertAudio(null)
            }

            if (isVibrationOn) {
                try {
                    vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.let { vib ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val vibrationPattern = longArrayOf(0, 420, 250, 420, 250, 420)
                            val indexToRepeat = 1 // repeat from first vibration burst
                            vib.vibrate(VibrationEffect.createWaveform(vibrationPattern, indexToRepeat))
                        } else {
                            @Suppress("DEPRECATION")
                            vib.vibrate(longArrayOf(0, 420, 250, 420, 250, 420), 1)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReminderAlertActivity", "Fails to trigger vibration: ${e.message}")
                }
            }
        }

        setContent {
            MyApplicationTheme {
                ReminderAlertScreenContent(
                    repository = repository,
                    onFinished = {
                        stopVibration()
                        stopAudio()
                        finish()
                    }
                )
            }
        }
    }

    private fun startAlertAudio(soundUriString: String?) {
        try {
            val uri = if (!soundUriString.isNullOrEmpty()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d("ReminderAlertActivity", "Successfully playing alert audio loop")
        } catch (e: Exception) {
            Log.e("ReminderAlertActivity", "Failed to start custom alert sound: ${e.message}")
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (fallbackUri != null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(applicationContext, fallbackUri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                }
            } catch (inner: Exception) {
                Log.e("ReminderAlertActivity", "Default sound fallback failed: ${inner.message}")
            }
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("ReminderAlertActivity", "Error stopping media player: ${e.message}")
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("ReminderAlertActivity", "Error stopping vibrator: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop vibration/audio if user navigates away or starts another flow
        stopVibration()
        stopAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibration()
        stopAudio()
    }
}

@Composable
fun ReminderAlertScreenContent(
    repository: HydrationRepository,
    onFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Observe settings and logs reactively from database
    val settingsState = repository.userSettings.collectAsState(initial = UserSettings())
    val allLogsState = repository.allLogs.collectAsState(initial = emptyList())

    val settings = settingsState.value
    val logs = allLogsState.value

    // Calculate start of today for progress stats
    val startOfToday = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todayLogs = remember(logs) {
        logs.filter { it.timestamp >= startOfToday }
    }

    val todayTotalMl = remember(todayLogs) {
        todayLogs.sumOf { it.amountMl }
    }

    val glassSize = settings.glassSizeMl.coerceAtLeast(1)
    val dailyGoal = settings.dailyGoalMl.coerceAtLeast(100)

    val progressPercent = (todayTotalMl.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
    val drankGlasses = todayTotalMl / glassSize
    val goalGlasses = dailyGoal / glassSize

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyDeep)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // TOP SECTION: Title and Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Time to Drink",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Drink Water 💧",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = SapphireBlue,
                    letterSpacing = (-1.2).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your body will thank you!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTextSecondary
                )
            }

            // CENTER SECTION: Animated Water Bottle rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                WaterBottleCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("alert_water_bottle_canvas"),
                    progress = progressPercent
                )
            }

            // PROGRESS CARD: Glassmorphism layout details
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassCardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("alert_progress_stats_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Today's Progress",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$drankGlasses / $goalGlasses Glasses",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fL / %.1fL", todayTotalMl / 1000f, dailyGoal / 1000f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireBlue
                        )
                    }

                    // Circular Progress Graphic Indicator matching Google style
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(68.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = 1.0f,
                            color = Color.White.copy(alpha = 0.08f),
                            strokeWidth = 5.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(
                            progress = progressPercent,
                            color = SapphireBlue,
                            strokeWidth = 5.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BOTTOM CONTROL SECTION: Primary, Secondary and Dismiss buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Primary action: ✓ I Drank Water
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            val newLog = HydrationLog(amountMl = glassSize)
                            repository.insertLog(newLog)

                            // Automatically schedule the next scheduled reminder
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = newLog.timestamp
                            cal.add(Calendar.MINUTE, settings.intervalMinutes)

                            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                            val nextTimeStr = sdf.format(cal.time)

                            repository.updateUserSettings(settings.copy(nextReminderTime = nextTimeStr))
                            if (settings.isReminderEnabled) {
                                AlarmScheduler.scheduleReminder(context, cal.timeInMillis)
                            }
                            
                            Toast.makeText(context, "Logged ${glassSize}ml successfully! 💧", Toast.LENGTH_SHORT).show()
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SapphireBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("alert_drank_water_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "✓ I Drank Water",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary action: ⏰ Snooze 10 Minutes
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MINUTE, 10)

                            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                            val snoozeTimeStr = sdf.format(cal.time)

                            // Save designated snooze as the next reminder
                            repository.updateUserSettings(settings.copy(nextReminderTime = snoozeTimeStr))
                            
                            // Schedule target exact snooze alarm
                            AlarmScheduler.scheduleReminder(context, cal.timeInMillis)

                            Toast.makeText(context, "Snoozed for 10 minutes ⏰", Toast.LENGTH_SHORT).show()
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassHighlight,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.2.dp, GlassCardBorder, RoundedCornerShape(26.dp))
                        .testTag("alert_snooze_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "⏰ Snooze 10 Minutes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Dismiss Action Link
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFinished()
                    },
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("alert_dismiss_button")
                ) {
                    Text(
                        text = "Dismiss",
                        color = GlassTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
