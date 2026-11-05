package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HydrationLog>>

    @Query("SELECT * FROM hydration_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLogDirectly(): HydrationLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HydrationLog)

    @Delete
    suspend fun deleteLog(log: HydrationLog)

    @Query("DELETE FROM hydration_logs")
    suspend fun clearAllLogs()

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)
}
