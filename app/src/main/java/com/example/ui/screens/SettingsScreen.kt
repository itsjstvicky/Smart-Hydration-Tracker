package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.app.NotificationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HydrationViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: HydrationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()

    var editingName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }

    var isFullScreenIntentAllowed by remember { mutableStateOf(true) }
    var isOverlayPermissionAllowed by remember { mutableStateOf(true) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isFullScreenIntentAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.canUseFullScreenIntent()
                } else {
                    true
                }
                isOverlayPermissionAllowed = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Launcher for system ringtone picker
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                ?: result.data?.data
            uri?.let {
                viewModel.updateReminderSoundUri(it.toString())
                Toast.makeText(context, "Audio selected successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for local custom audio file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // Secure persistable URI permission so we can read it again later safely
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                android.util.Log.e("SettingsScreen", "Failed to take persistable URI permission: ${e.message}")
            }
            viewModel.updateReminderSoundUri(it.toString())
            Toast.makeText(context, "Custom audio file loaded successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // TITLE HEADER
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                text = "Preferences",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Configure app goals and timers",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlassTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // PROFILE CONFIG CARD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_config_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNameDialog = true }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SapphireBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = SapphireBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Display Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = settings.userName,
                            fontSize = 13.sp,
                            color = GlassTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 12.sp,
                        color = SapphireBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = GlassTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // GOAL CALIBRATIONS SECTION
        Text(
            text = "Daily Hydration Goals",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().testTag("goals_config_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Goal Slider Metric
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WineBar,
                            contentDescription = null,
                            tint = SapphireBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Intake Goal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", settings.dailyGoalMl / 1000f)} Liters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SapphireBlue
                    )
                }

                Slider(
                    value = settings.dailyGoalMl.toFloat(),
                    onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                    valueRange = 1000f..4000f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = SapphireBlue,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("daily_goal_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1.0L", fontSize = 10.sp, color = GlassTextSecondary)
                    Text(text = "2.0L", fontSize = 10.sp, color = GlassTextSecondary)
                    Text(text = "3.0L", fontSize = 10.sp, color = GlassTextSecondary)
                    Text(text = "4.0L", fontSize = 10.sp, color = GlassTextSecondary)
                }

                Divider(
                    color = Color.White.copy(alpha = 0.05f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Glass Size Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            tint = SapphireBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Water Glass Size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${settings.glassSizeMl} ml",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SapphireBlue
                    )
                }

                // Horizontal Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val presets = listOf(200, 250, 330, 500)
                    presets.forEach { size ->
                        val selected = settings.glassSizeMl == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) SapphireBlue.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = if (selected) SapphireBlue else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateGlassSize(size)
                                    Toast
                                        .makeText(
                                            context,
                                            "Glass size set to ${size}ml",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${size}ml",
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else GlassTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // TIMERS AND ALARMS CONFIGURATION
        Text(
            text = "Alarm & Schedule",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().testTag("alarms_config_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Toggle switch row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setReminderEnabled(!settings.isReminderEnabled) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Intake Reminders",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (settings.isReminderEnabled) "On" else "Off",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.isReminderEnabled,
                        onCheckedChange = { viewModel.setReminderEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SapphireBlue
                        )
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // Vibration Alert Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setVibrationEnabled(!settings.isVibrationEnabled) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Vibration Alert",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (settings.isVibrationEnabled) "Vibrate on alert" else "Silent alert",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.isVibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SapphireBlue
                        )
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // Notification Tray Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setNotificationEnabled(!settings.isNotificationEnabled) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Status Notifications",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (settings.isNotificationEnabled) "On" else "Off",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.isNotificationEnabled,
                        onCheckedChange = { viewModel.setNotificationEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SapphireBlue
                        )
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // Select Next Reminder Clock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Next Reminder Target",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Calculated automatically from last logged drink",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Text(
                        text = settings.nextReminderTime,
                        fontSize = 14.sp,
                        color = SapphireBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // Reminder Interval
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SapphireBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassEmpty,
                                    contentDescription = null,
                                    tint = SapphireBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Reminder Interval",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        val intervalLabel = if (settings.intervalMinutes < 60) {
                            "Every ${settings.intervalMinutes} Minutes"
                        } else {
                            val hours = settings.intervalMinutes / 60
                            "Every $hours " + (if (hours == 1) "Hour" else "Hours")
                        }
                        Text(
                            text = intervalLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphireBlue
                        )
                    }

                    val intervalOptions = listOf(
                        10 to "10m",
                        30 to "30m",
                        60 to "1h",
                        120 to "2h",
                        180 to "3h",
                        240 to "4h"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervalOptions.chunked(3).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunk.forEach { (minutes, label) ->
                                    val selected = settings.intervalMinutes == minutes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) SapphireBlue.copy(alpha = 0.15f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (selected) SapphireBlue else Color.White.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                viewModel.updateReminderInterval(minutes)
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color.White else GlassTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // Reminder Sound Row Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSoundDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Reminder Sound",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val soundName = remember(settings.reminderSoundUri) {
                                getRingtoneName(context, settings.reminderSoundUri)
                            }
                            Text(
                                text = soundName,
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Select sound",
                        tint = GlassTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // LOCK SCREEN COMPATIBILITY & SUPPORT CARD
        Text(
            text = "Lock Screen Compatibility",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SapphireBlue,
            modifier = Modifier.padding(top = 16.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().testTag("lockscreen_config_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Info description banner
                Text(
                    text = "Android protects lockscreen security of your phone by blocking background pop-ups by default. Grant the options below to ensure full-screen drink water alerts immediately overlay your screen.",
                    fontSize = 11.sp,
                    color = GlassTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    lineHeight = 16.sp
                )

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // 1. Show Alarms on Lock Screen (Full screen intent permission)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "Could not open Full-Screen settings screen.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Lock-screen alerts are always supported on your Android version natively!", Toast.LENGTH_LONG).show()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fullscreen,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show on Locked Screen",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) "Required on Android 14+" else "Allowed natively",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    // Status Badge
                    val fsiAllowed = isFullScreenIntentAllowed
                    Surface(
                        color = if (fsiAllowed) Color(0x1F4CAF50) else Color(0x1FFF9800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (fsiAllowed) "Allowed" else "Requires Action",
                            color = if (fsiAllowed) Color(0xFF8BC34A) else Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // 2. Display Over Other Apps (Overlay permission)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "Could not open Overlay settings screen.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Display Over Other Apps",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Overrides Active Screens",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    // Status Badge
                    val overlayAllowed = isOverlayPermissionAllowed
                    Surface(
                        color = if (overlayAllowed) Color(0x1F4CAF50) else Color(0x1FFF9800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (overlayAllowed) "Allowed" else "Requires Action",
                            color = if (overlayAllowed) Color(0xFF8BC34A) else Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.03f), thickness = 1.dp)

                // 3. System App Info Shortcut (Bypassing OEM overlay/startup restrictions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open App details settings.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SapphireBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Other OEM Permissions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Xiaomi/Huawei: toggle 'Show on Lock Screen'",
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open App Info",
                        tint = GlassTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // RESET CORE DATA
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.clearAllLogs()
                Toast.makeText(context, "All logged fluid data reset successfully", Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("reset_data_button")
        ) {
            Icon(
                imageVector = Icons.Filled.SettingsBackupRestore,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 8.dp).size(16.dp)
            )
            Text(
                text = "Reset All Daily Log Progress",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // POPUP MODAL DIALOGS FOR SETTINGS CHANGER
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Display Name", color = Color.White) },
                text = {
                    Column {
                        Text(
                            text = "Customize how the app greets you in the morning.",
                            fontSize = 12.sp,
                            color = GlassTextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SapphireBlue,
                                unfocusedBorderColor = GlassTextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editingName.isNotBlank()) {
                                viewModel.updateUserName(editingName.trim())
                                Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT).show()
                            }
                            showNameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel", color = GlassTextSecondary)
                    }
                },
                containerColor = NavyDeep,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showSoundDialog) {
            AlertDialog(
                onDismissRequest = { showSoundDialog = false },
                title = { Text("Choose Reminder Sound 🎵", color = Color.White) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select an audio source for your water hydration alarms.",
                            fontSize = 13.sp,
                            color = GlassTextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        // Option 1: System Tones / Built-in
                        Button(
                            onClick = {
                                showSoundDialog = false
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Sound Tone")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    if (!settings.reminderSoundUri.isNullOrEmpty()) {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(settings.reminderSoundUri))
                                    }
                                }
                                ringtonePickerLauncher.launch(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, SapphireBlue.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Audiotrack,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Ringtones & Tones", color = Color.White, fontSize = 14.sp)
                        }

                        // Option 2: Local Audio File
                        Button(
                            onClick = {
                                showSoundDialog = false
                                filePickerLauncher.launch("audio/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, SapphireBlue.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Music / Audio File", color = Color.White, fontSize = 14.sp)
                        }

                        // Option 3: Reset to Default Sound
                        if (!settings.reminderSoundUri.isNullOrEmpty()) {
                            TextButton(
                                onClick = {
                                    showSoundDialog = false
                                    viewModel.updateReminderSoundUri(null)
                                    Toast.makeText(context, "Reset to default notification sound", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset to Default Sound", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSoundDialog = false }) {
                        Text("Cancel", color = GlassTextSecondary)
                    }
                },
                containerColor = NavyDeep,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

fun getRingtoneName(context: Context, uriString: String?): String {
    if (uriString.isNullOrEmpty()) return "Default Tone"
    return try {
        val uri = Uri.parse(uriString)
        if (uriString.contains("content://media/external/file") || 
            uriString.contains("content://com.android.providers.downloads") ||
            uriString.contains("content://com.android.externalstorage.documents")
        ) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && it.moveToFirst()) {
                    return it.getString(nameIndex)
                }
            }
            "Selected Audio File"
        } else {
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.getTitle(context) ?: "Custom Notification Tone"
        }
    } catch (e: Exception) {
        "Custom Notification Tone"
    }
}
