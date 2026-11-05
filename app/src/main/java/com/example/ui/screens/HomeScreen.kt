package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaterBottleCanvas
import com.example.ui.viewmodel.HydrationViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HydrationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val todayMl by viewModel.todayTotalMl.collectAsState()
    val glassesFloat by viewModel.todayGlasses.collectAsState()
    val progressPercent by viewModel.progressPercent.collectAsState()
    
    // Dynamic Date Formatting
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Random Hello/Hey friendly Greeting
    val greetingPrefix = remember {
        listOf("Hey", "Hi", "Hello", "Vanakkam", "Yo").random()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP SECTION: GREETINGS & NOTIFICATIONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greetingPrefix, ${settings.userName} 👋",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = currentDateStr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Notification toggle status shortcut inside primary view
            IconButton(
                onClick = {
                    val toggled = !settings.isReminderEnabled
                    viewModel.setReminderEnabled(toggled)
                    val msg = if (toggled) "Water Reminders Activated!" else "Reminders Muted"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassWhite)
                    .border(1.dp, GlassCardBorder, CircleShape)
                    .size(46.dp)
                    .testTag("notification_toggle_top_button")
            ) {
                Icon(
                    imageVector = if (settings.isReminderEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = "Quick Alarm Config",
                    tint = if (settings.isReminderEnabled) SapphireBlue else GlassTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Subtitle slogan inspired by high fidelity dashboard
        Text(
            text = "Stay hydrated, stay healthy.",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SapphireBlue,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        // HERO SECTION: 3D GLASS WATER BOTTLE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            WaterBottleCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("water_bottle_canvas"),
                progress = progressPercent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PROGRESS CARD: Glassmorphic summary stats card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassCardBg)
                .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
                .testTag("progress_summary_card"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Glass Cup custom icon representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SapphireBlue.copy(alpha = 0.15f))
                        .border(1.dp, SapphireBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Glass indicator",
                        tint = SapphireBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    val completedGlasses = glassesFloat.toInt()
                    val targetGlasses = (settings.dailyGoalMl / settings.glassSizeMl)
                    Text(
                        text = "$completedGlasses / $targetGlasses Glasses Completed",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", todayMl / 1000f)}L / ${String.format(Locale.US, "%.1f", settings.dailyGoalMl / 1000f)}L",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlassTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Radial progress tracking badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progressPercent },
                    color = SapphireBlue,
                    strokeWidth = 5.dp,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // REMINDER CARD: Alarm aesthetic Google Clock inspired card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (settings.isReminderEnabled) NavyLight.copy(alpha = 0.7f) else GlassWhite)
                .border(
                    width = 1.dp,
                    color = if (settings.isReminderEnabled) SapphireBlue.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {
                    viewModel.setReminderEnabled(!settings.isReminderEnabled)
                }
                .padding(18.dp)
                .testTag("reminder_config_card"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (settings.isReminderEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = "Timer type",
                        tint = if (settings.isReminderEnabled) SapphireBlue else GlassTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Next Reminder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (settings.isReminderEnabled) SapphireBlue else GlassTextSecondary
                    )
                }
                
                Text(
                    text = settings.nextReminderTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (settings.isReminderEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    letterSpacing = 0.5.sp
                )
                
                val intervalText = if (settings.intervalMinutes < 60) {
                    "Every ${settings.intervalMinutes} Minutes"
                } else {
                    val hours = settings.intervalMinutes / 60
                    "Every $hours " + (if (hours == 1) "Hour" else "Hours")
                }
                Text(
                    text = intervalText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTextSecondary
                )
            }

            // Google Clock inspired alarm status slider / switch toggle
            Switch(
                checked = settings.isReminderEnabled,
                onCheckedChange = { viewModel.setReminderEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SapphireBlue,
                    uncheckedThumbColor = GlassTextSecondary,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.testTag("reminder_active_switch")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PRIMARY DRINK WATER ACTION SECTION
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rounded outer neon pulsing glow card around the ring button
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = SapphireBlue,
                        spotColor = SapphireBlue
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.addWaterLog()
                        Toast.makeText(context, "+${settings.glassSizeMl}ml Drank!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(86.dp)
                        .testTag("drank_water_fab"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SapphireBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Drink Water Action Launcher",
                        modifier = Modifier.size(38.dp),
                        tint = Color.White
                    )
                }
            }
            
            Text(
                text = "Drank Water",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = "Tap to record ${settings.glassSizeMl}ml",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = GlassTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Subtract helper shortcut to quickly fix mistakes
            if (todayMl > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.removeLastWaterLog()
                            Toast.makeText(context, "Last glass removed", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Undo drink",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Subtract Last",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
