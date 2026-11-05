package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_logs")
data class HydrationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Int
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Arjun",
    val dailyGoalMl: Int = 2000,
    val glassSizeMl: Int = 250,
    val nextReminderTime: String = "03:30 PM",
    val isReminderEnabled: Boolean = true,
    val intervalMinutes: Int = 60,
    val reminderSoundUri: String? = null,
    val isVibrationEnabled: Boolean = true,
    val isNotificationEnabled: Boolean = true
)
