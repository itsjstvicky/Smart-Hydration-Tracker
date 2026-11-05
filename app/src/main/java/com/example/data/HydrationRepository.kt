package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HydrationRepository(private val dao: HydrationDao) {
    val allLogs: Flow<List<HydrationLog>> = dao.getAllLogs()

    val userSettings: Flow<UserSettings> = dao.getUserSettingsFlow().map { settings ->
        settings ?: UserSettings() // Default values if none initialized yet
    }

    suspend fun getDirectUserSettings(): UserSettings {
        return dao.getUserSettingsDirect() ?: UserSettings()
    }

    suspend fun getLatestLogDirectly(): HydrationLog? {
        return dao.getLatestLogDirectly()
    }

    suspend fun insertLog(log: HydrationLog) {
        dao.insertLog(log)
    }

    suspend fun deleteLog(log: HydrationLog) {
        dao.deleteLog(log)
    }

    suspend fun clearLogs() {
        dao.clearAllLogs()
    }

    suspend fun updateUserSettings(settings: UserSettings) {
        dao.insertUserSettings(settings)
    }
}
