package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ActiveTab {
    HOME, STATISTICS, SETTINGS
}

class HydrationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HydrationDatabase.getDatabase(application)
    private val repository = HydrationRepository(database.dao)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(ActiveTab.HOME)
    val currentTab: StateFlow<ActiveTab> = _currentTab.asStateFlow()

    // Persistent User Settings
    val settingsState: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    // All historic logs
    val allLogs: StateFlow<List<HydrationLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Prepare initial settings row if it doesn't exist
        viewModelScope.launch {
            // Loading direct user settings forces creation if missing
            val direct = repository.getDirectUserSettings()
            if (direct.nextReminderTime == "03:30 PM") {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MINUTE, direct.intervalMinutes)
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                repository.updateUserSettings(direct.copy(nextReminderTime = sdf.format(cal.time)))
            } else {
                repository.updateUserSettings(direct)
            }
        }
    }

    // Helper: start of today in timestamp
    private fun getStartOfDayMillis(daysOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Helper: end of today in timestamp
    private fun getEndOfDayMillis(daysOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    // Filtered logs for today
    val todayLogs: StateFlow<List<HydrationLog>> = repository.allLogs
        .map { logs ->
            val startOfToday = getStartOfDayMillis(0)
            logs.filter { it.timestamp >= startOfToday }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sum of ML drank today
    val todayTotalMl: StateFlow<Int> = todayLogs
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Number of completed glasses today based on glass size
    val todayGlasses: StateFlow<Float> = combine(todayTotalMl, settingsState) { total, settings ->
        val glassSize = settings.glassSizeMl.coerceAtLeast(1)
        total.toFloat() / glassSize.toFloat()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    // Hydration progress percent
    val progressPercent: StateFlow<Float> = combine(todayTotalMl, settingsState) { total, settings ->
        val goal = settings.dailyGoalMl.coerceAtLeast(100)
        (total.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    // Past 7 Days Stat Bars (Day Label to IntakeML)
    data class DailyStat(val dayLabel: String, val amountMl: Int, val goalMl: Int, val percent: Float)

    val last7DaysStats: StateFlow<List<DailyStat>> = combine(repository.allLogs, settingsState) { logs, settings ->
        val stats = mutableListOf<DailyStat>()
        val weekdayLabels = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        for (i in 6 downTo 0) {
            val start = getStartOfDayMillis(i)
            val end = getEndOfDayMillis(i)
            
            // Filter logs for this specific day offsets
            val daySum = logs.filter { it.timestamp in start..end }.sumOf { it.amountMl }
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 to 7
            val label = if (i == 0) "Today" else weekdayLabels[dayOfWeek - 1]
            
            val goal = settings.dailyGoalMl.coerceAtLeast(100)
            val pct = (daySum.toFloat() / goal.toFloat()).coerceIn(0f, 2f) // Allow up to 200% on chart
            
            stats.add(DailyStat(label, daySum, goal, pct))
        }
        stats
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI actions
    fun setTab(tab: ActiveTab) {
        _currentTab.value = tab
    }

    fun addWaterLog() {
        viewModelScope.launch {
            val settings = settingsState.value
            val log = HydrationLog(amountMl = settings.glassSizeMl)
            repository.insertLog(log)
            
            // Calculate next reminder: current logged timestamp + intervalMinutes
            val cal = Calendar.getInstance()
            cal.timeInMillis = log.timestamp
            cal.add(Calendar.MINUTE, settings.intervalMinutes)
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val nextTime = sdf.format(cal.time)
            
            repository.updateUserSettings(settings.copy(nextReminderTime = nextTime))
            if (settings.isReminderEnabled) {
                com.example.receiver.AlarmScheduler.scheduleReminder(getApplication(), cal.timeInMillis)
            }
        }
    }

    fun removeLastWaterLog() {
        viewModelScope.launch {
            val logs = todayLogs.value
            if (logs.isNotEmpty()) {
                // Delete latest log of today
                repository.deleteLog(logs.first())
                
                // Recalculate next reminder based on previous logged water today, or current time
                val remainingLogs = logs.drop(1)
                val settings = settingsState.value
                val cal = Calendar.getInstance()
                if (remainingLogs.isNotEmpty()) {
                    cal.timeInMillis = remainingLogs.first().timestamp
                } else {
                    cal.timeInMillis = System.currentTimeMillis()
                }
                cal.add(Calendar.MINUTE, settings.intervalMinutes)
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                val nextTime = sdf.format(cal.time)
                repository.updateUserSettings(settings.copy(nextReminderTime = nextTime))
                if (settings.isReminderEnabled) {
                    com.example.receiver.AlarmScheduler.scheduleReminder(getApplication(), cal.timeInMillis)
                } else {
                    com.example.receiver.AlarmScheduler.cancelReminder(getApplication())
                }
            }
        }
    }

    fun deleteWaterLog(log: HydrationLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(userName = name))
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(dailyGoalMl = goalMl.coerceAtLeast(100)))
        }
    }

    fun updateGlassSize(sizeMl: Int) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(glassSizeMl = sizeMl.coerceAtLeast(50)))
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(isReminderEnabled = enabled))
            if (enabled) {
                val logs = todayLogs.value
                val cal = Calendar.getInstance()
                if (logs.isNotEmpty()) {
                    cal.timeInMillis = logs.first().timestamp
                } else {
                    cal.timeInMillis = System.currentTimeMillis()
                }
                cal.add(Calendar.MINUTE, current.intervalMinutes)
                com.example.receiver.AlarmScheduler.scheduleReminder(getApplication(), cal.timeInMillis)
            } else {
                com.example.receiver.AlarmScheduler.cancelReminder(getApplication())
            }
        }
    }

    fun updateReminderTime(timeString: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(nextReminderTime = timeString))
        }
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            val current = settingsState.value
            val newInterval = minutes.coerceAtLeast(1)
            
            val logs = todayLogs.value
            val cal = Calendar.getInstance()
            if (logs.isNotEmpty()) {
                cal.timeInMillis = logs.first().timestamp
            } else {
                cal.timeInMillis = System.currentTimeMillis()
            }
            cal.add(Calendar.MINUTE, newInterval)
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val nextTime = sdf.format(cal.time)
            
            repository.updateUserSettings(current.copy(intervalMinutes = newInterval, nextReminderTime = nextTime))
            if (current.isReminderEnabled) {
                com.example.receiver.AlarmScheduler.scheduleReminder(getApplication(), cal.timeInMillis)
            }
        }
    }

    fun updateReminderSoundUri(uri: String?) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(reminderSoundUri = uri))
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(isVibrationEnabled = enabled))
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateUserSettings(current.copy(isNotificationEnabled = enabled))
        }
    }
}
