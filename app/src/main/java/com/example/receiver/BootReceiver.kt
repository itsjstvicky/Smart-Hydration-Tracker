package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.HydrationDatabase
import com.example.data.HydrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Restoring alarm reminders.")
            val appContext = context.applicationContext
            val db = HydrationDatabase.getDatabase(appContext)
            val repo = HydrationRepository(db.dao)

            CoroutineScope(Dispatchers.IO).launch {
                val settings = repo.getDirectUserSettings()
                if (settings.isReminderEnabled) {
                    val latestLog = repo.getLatestLogDirectly()
                    val cal = Calendar.getInstance()
                    if (latestLog != null) {
                        cal.timeInMillis = latestLog.timestamp
                    } else {
                        cal.timeInMillis = System.currentTimeMillis()
                    }
                    cal.add(Calendar.MINUTE, settings.intervalMinutes)
                    
                    // Reschedule with AlarmScheduler
                    AlarmScheduler.scheduleReminder(appContext, cal.timeInMillis)
                }
            }
        }
    }
}
