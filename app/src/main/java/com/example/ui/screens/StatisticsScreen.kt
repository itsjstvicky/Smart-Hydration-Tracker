package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HydrationViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: HydrationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stats by viewModel.last7DaysStats.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    // Calculate dynamic stats summary values
    val avgMl = remember(stats) {
        if (stats.isEmpty()) 0 else stats.map { it.amountMl }.average().toInt()
    }
    val goalMetDays = remember(stats) {
        stats.count { it.amountMl >= it.goalMl }
    }
    // Calculate current streak
    val streakDays = remember(stats) {
        var streak = 0
        // Evaluate backward from today index (last element)
        for (i in stats.indices.reversed()) {
            if (stats[i].amountMl >= stats[i].goalMl) {
                streak++
            } else {
                if (i < stats.size - 1) { // If it's not today, break streak
                    break
                }
            }
        }
        streak
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("statistics_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // HEADER TITLE
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Hydration Stats",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Keep an eye on your patterns",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // GOAL MET STREAK HERO CARD
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassCardBg)
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("streak_statistics_card"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (streakDays > 0) Color(0xFFFF9800).copy(alpha = 0.15f)
                            else SapphireBlue.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (streakDays > 0) Icons.Filled.LocalFireDepartment else Icons.Outlined.WaterDrop,
                        contentDescription = "Streak Fire",
                        tint = if (streakDays > 0) Color(0xFFFF9800) else SapphireBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (streakDays > 0) "$streakDays Day Streak! 🔥" else "Start a Streak",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Met goal: $goalMetDays of past 7 days",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = GlassTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", avgMl / 1000f)}L",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SapphireBlue
                    )
                    Text(
                        text = "Weekly Avg",
                        fontSize = 11.sp,
                        color = GlassTextSecondary
                    )
                }
            }
        }

        // PAST 7 DAYS VISUAL BAR CHART CONTAINER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_bars_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "7-Day Habits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Custom Canvas chart drawing - highly polished, Dribbble quality
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val paddingLeft = 35.dp.toPx()
                            val paddingBottom = 20.dp.toPx()
                            val chartW = w - paddingLeft
                            val chartH = h - paddingBottom

                            // 1. Draw horizontal goal reference dashed line running across chart
                            val goalY = chartH * 0.45f // Scale goal line level
                            drawLine(
                                color = SapphireBlue.copy(alpha = 0.45f),
                                start = Offset(paddingLeft, goalY),
                                end = Offset(w, goalY),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                            )

                            // 2. Plot past 7 days bars
                            if (stats.isNotEmpty()) {
                                val itemWidth = chartW / stats.size
                                val barWidth = itemWidth * 0.45f

                                stats.forEachIndexed { index, day ->
                                    val barCenterX = paddingLeft + (index * itemWidth) + (itemWidth / 2f)
                                    
                                    // Scale height proportionally
                                    // day.percent: 1f means goal reached. Represent goal as 45% height
                                    val barHeight = (chartH * 0.7f * day.percent).coerceIn(4.dp.toPx(), chartH * 0.95f)
                                    val barTopY = chartH - barHeight

                                    // Check if target was verified
                                    val met = day.amountMl >= day.goalMl
                                    val barBrush = if (met) {
                                        Brush.verticalGradient(
                                            colors = listOf(SapphireBlue, AzureBlue)
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                Color.White.copy(alpha = 0.06f)
                                            )
                                        )
                                    }

                                    // Draw the bar columns with custom top rounded corner
                                    drawRoundRect(
                                        brush = barBrush,
                                        topLeft = Offset(barCenterX - barWidth / 2f, barTopY),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                                    )

                                    if (met) {
                                        // Top highlight cap glow
                                        drawCircle(
                                            color = SapphireBlue,
                                            radius = 3.dp.toPx(),
                                            center = Offset(barCenterX, barTopY)
                                        )
                                    }

                                    // Write day text labels centered at bottom
                                    drawContext.canvas.nativeCanvas.apply {
                                        val p = android.graphics.Paint().apply {
                                            color = if (index == stats.size - 1) 0xFF4A8CFF.toInt() else 0xFF8FA1BC.toInt()
                                            textSize = 10.dp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                                        }
                                        drawText(
                                            day.dayLabel,
                                            barCenterX,
                                            h - 4.dp.toPx(),
                                            p
                                        )

                                        // Label value overlay at top of bar (volume or ml metric)
                                        val valP = android.graphics.Paint().apply {
                                            color = if (met) 0xFFFFFFFF.toInt() else 0xFF7A8DAB.toInt()
                                            textSize = 8.dp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        val valStr = if (day.amountMl >= 1000) "${String.format(Locale.US, "%.1f", day.amountMl / 1000f)}L" else "${day.amountMl}"
                                        drawText(
                                            valStr,
                                            barCenterX,
                                            barTopY - 6.dp.toPx(),
                                            valP
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SapphireBlue)
                            )
                            Text(text = "Goal met", fontSize = 11.sp, color = GlassTextSecondary)
                        }

                        Text(
                            text = "Target Line: ${String.format(Locale.US, "%.1f", settings.dailyGoalMl / 1000f)}L",
                            fontSize = 11.sp,
                            color = SapphireBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // CHRONOLOGICAL WATER LOGS SECTION
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Logs",
                        tint = SapphireBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Today's Intake Log",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (todayLogs.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.clearAllLogs()
                            Toast.makeText(context, "Cleared today's hydration logs", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
                    ) {
                        Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dynamic log list items
        if (todayLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassWhite)
                        .padding(24.dp)
                        .testTag("empty_stats_logs"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = "Empty",
                            tint = GlassTextSecondary,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "No records yet today.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "Click 'Drank Water' on Home to start logging!",
                            fontSize = 12.sp,
                            color = GlassTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(todayLogs, key = { it.id }) { log ->
                val timeStr = remember(log.timestamp) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    sdf.format(Date(log.timestamp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassCardBg)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .animateItem()
                        .testTag("log_item_${log.id}"),
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
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                tint = SapphireBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Logged Glass of Water",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = timeStr,
                                fontSize = 11.sp,
                                color = GlassTextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${log.amountMl} ml",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphireBlue
                        )

                        // Delete button to correct data locally
                        IconButton(
                            onClick = {
                                viewModel.deleteWaterLog(log)
                                Toast.makeText(context, "${log.amountMl}ml removed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete log",
                                tint = Color.Red.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
