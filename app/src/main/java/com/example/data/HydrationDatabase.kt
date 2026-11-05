package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HydrationLog::class, UserSettings::class], version = 4, exportSchema = false)
abstract class HydrationDatabase : RoomDatabase() {
    abstract val dao: HydrationDao

    companion object {
        @Volatile
        private var INSTANCE: HydrationDatabase? = null

        fun getDatabase(context: Context): HydrationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydrationDatabase::class.java,
                    "hydration_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
